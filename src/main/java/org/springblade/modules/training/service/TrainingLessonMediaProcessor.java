package org.springblade.modules.training.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.oss.model.BladeFile;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.resource.builder.OssBuilder;
import org.springblade.modules.traininglesson.mapper.TrainingLessonMapper;
import org.springblade.modules.traininglesson.pojo.entity.TrainingLessonEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 培训长视频课时媒体处理器。
 *
 * <p>使用 FFmpeg 直接读取远程视频生成封面，避免在应用服务器完整下载长视频；
 * 使用 FFprobe 补齐视频时长。生产环境仍建议接入专门转码服务生成 HLS/多清晰度资源。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingLessonMediaProcessor {

	private static final int MAX_PROCESS_OUTPUT_LENGTH = 8000;

	private final TrainingLessonMapper lessonMapper;
	private final OssBuilder ossBuilder;

	@Value("${media.ffmpeg-path:ffmpeg}")
	private String ffmpegPath;

	@Value("${media.ffprobe-path:ffprobe}")
	private String ffprobePath;

	@Value("${media.temp-dir:/data/tmp/video-cover}")
	private String tempDir;

	@Async("videoCoverExecutor")
	public void processAsync(Long lessonId) {
		TrainingLessonEntity lesson = lessonMapper.selectById(lessonId);
		if (lesson == null || Func.equals(lesson.getIsDeleted(), 1) || Func.isBlank(lesson.getVideoUrl())) {
			log.warn("跳过课程课时媒体处理，课时不存在、已删除或视频地址为空，lessonId={}", lessonId);
			return;
		}

		String videoUrl = lesson.getVideoUrl();
		Path coverPath = null;
		log.info("开始处理课程课时媒体，lessonId={}，videoUrl={}，ffmpegPath={}，ffprobePath={}，tempDir={}",
			lessonId, videoUrl, ffmpegPath, ffprobePath, tempDir);
		markStatus(lessonId, videoUrl, "PROCESSING", null, null);

		try {
			Path directory = Paths.get(tempDir, "training-lesson");
			Files.createDirectories(directory);
			String uuid = UUID.randomUUID().toString().replace("-", "");
			coverPath = directory.resolve(uuid + ".jpg");

			if (!generateCover(videoUrl, coverPath)) {
				log.error("课程课时封面生成失败，lessonId={}，videoUrl={}，coverPath={}", lessonId, videoUrl, coverPath);
				markStatus(lessonId, videoUrl, "FAILED", null, null);
				return;
			}
			long coverSize = Files.size(coverPath);
			log.info("课程课时封面生成成功，lessonId={}，coverPath={}，fileSize={}", lessonId, coverPath, coverSize);

			TrainingLessonEntity latest = lessonMapper.selectById(lessonId);
			if (latest == null) {
				log.warn("媒体处理过程中课时已不存在，放弃后续上传，lessonId={}", lessonId);
				return;
			}
			if (!videoUrl.equals(latest.getVideoUrl())) {
				log.warn("媒体处理过程中视频地址已变化，放弃旧任务，lessonId={}，oldUrl={}，newUrl={}",
					lessonId, videoUrl, latest.getVideoUrl());
				return;
			}

			String tenantId = Func.isBlank(latest.getTenantId()) ? "000000" : latest.getTenantId();
			String objectName = "training/lesson-cover/"
				+ LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "/" + uuid + ".jpg";
			log.info("开始上传课程课时封面，lessonId={}，tenantId={}，objectName={}", lessonId, tenantId, objectName);

			BladeFile uploaded;
			try (InputStream input = Files.newInputStream(coverPath)) {
				uploaded = ossBuilder.template(tenantId, "").putFile(objectName, input);
			}
			if (uploaded == null) {
				log.error("课程课时封面上传失败，OSS返回结果为空，lessonId={}，objectName={}", lessonId, objectName);
				markStatus(lessonId, videoUrl, "FAILED", null, null);
				return;
			}
			if (Func.isBlank(uploaded.getLink())) {
				log.error("课程课时封面上传失败，OSS未返回文件地址，lessonId={}，objectName={}，uploaded={}",
					lessonId, objectName, uploaded);
				markStatus(lessonId, videoUrl, "FAILED", null, null);
				return;
			}
			log.info("课程课时封面上传成功，lessonId={}，posterUrl={}", lessonId, uploaded.getLink());

			Integer duration = probeDuration(videoUrl);
			if (duration == null || duration <= 0) {
				log.warn("课程课时视频时长读取失败，但封面已生成，lessonId={}，videoUrl={}", lessonId, videoUrl);
			}
			markStatus(lessonId, videoUrl, "READY", uploaded.getLink(), duration);
			log.info("课程课时媒体处理完成，lessonId={}，posterUrl={}，duration={}",
				lessonId, uploaded.getLink(), duration);
		} catch (Exception exception) {
			log.error("培训课时媒体处理异常，lessonId={}，videoUrl={}", lessonId, videoUrl, exception);
			markStatus(lessonId, videoUrl, "FAILED", null, null);
		} finally {
			if (coverPath != null) {
				try {
					Files.deleteIfExists(coverPath);
				} catch (Exception exception) {
					log.warn("删除培训课时临时封面失败，lessonId={}，coverPath={}", lessonId, coverPath, exception);
				}
			}
		}
	}

	private boolean generateCover(String videoUrl, Path coverPath) {
		Process process = null;
		StringBuilder output = new StringBuilder();
		try {
			ProcessBuilder builder = new ProcessBuilder(
				ffmpegPath,
				"-nostdin",
				"-y",
				"-ss", "00:00:01",
				"-i", videoUrl,
				"-frames:v", "1",
				"-update", "1",
				"-vf", "scale=960:-2",
				"-q:v", "3",
				coverPath.toAbsolutePath().toString()
			);
			builder.redirectErrorStream(true);
			log.info("执行FFmpeg课程封面命令，videoUrl={}，coverPath={}", videoUrl, coverPath);
			process = builder.start();
			Thread outputThread = consumeOutput(process, output, "training-ffmpeg-output");
			boolean finished = process.waitFor(60, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				joinQuietly(outputThread);
				log.error("FFmpeg生成课程封面超时，videoUrl={}，coverPath={}，output={}",
					videoUrl, coverPath, limitOutput(output));
				return false;
			}
			joinQuietly(outputThread);
			int exitCode = process.exitValue();
			if (exitCode != 0) {
				log.error("FFmpeg生成课程封面失败，exitCode={}，videoUrl={}，coverPath={}，output={}",
					exitCode, videoUrl, coverPath, limitOutput(output));
				return false;
			}
			if (!Files.exists(coverPath) || Files.size(coverPath) <= 0) {
				log.error("FFmpeg返回成功但封面文件不存在或为空，videoUrl={}，coverPath={}，output={}",
					videoUrl, coverPath, limitOutput(output));
				return false;
			}
			log.info("FFmpeg生成课程封面成功，videoUrl={}，coverPath={}，fileSize={}",
				videoUrl, coverPath, Files.size(coverPath));
			return true;
		} catch (Exception exception) {
			log.error("FFmpeg生成培训课时封面异常，ffmpegPath={}，videoUrl={}，coverPath={}，output={}",
				ffmpegPath, videoUrl, coverPath, limitOutput(output), exception);
			if (process != null) process.destroyForcibly();
			return false;
		}
	}

	private Integer probeDuration(String videoUrl) {
		Process process = null;
		StringBuilder output = new StringBuilder();
		try {
			ProcessBuilder builder = new ProcessBuilder(
				ffprobePath,
				"-v", "error",
				"-show_entries", "format=duration",
				"-of", "default=noprint_wrappers=1:nokey=1",
				videoUrl
			);
			builder.redirectErrorStream(true);
			process = builder.start();
			Thread outputThread = consumeOutput(process, output, "training-ffprobe-output");
			boolean finished = process.waitFor(45, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				joinQuietly(outputThread);
				log.warn("FFprobe读取课时时长超时，url={}，output={}", videoUrl, limitOutput(output));
				return null;
			}
			joinQuietly(outputThread);
			if (process.exitValue() != 0) {
				log.warn("FFprobe读取课时时长失败，exitCode={}，url={}，output={}",
					process.exitValue(), videoUrl, limitOutput(output));
				return null;
			}
			double seconds = Double.parseDouble(output.toString().trim());
			return Math.max(0, (int) Math.round(seconds));
		} catch (Exception exception) {
			log.warn("FFprobe读取课时时长异常，ffprobePath={}，url={}，output={}",
				ffprobePath, videoUrl, limitOutput(output), exception);
			if (process != null) process.destroyForcibly();
			return null;
		}
	}

	private void markStatus(Long lessonId,
							  String videoUrl,
							  String status,
							  String posterUrl,
							  Integer durationSeconds) {
		try {
			LambdaUpdateWrapper<TrainingLessonEntity> wrapper = Wrappers.<TrainingLessonEntity>lambdaUpdate()
				.eq(TrainingLessonEntity::getId, lessonId)
				.eq(TrainingLessonEntity::getVideoUrl, videoUrl)
				.set(TrainingLessonEntity::getMediaProcessStatus, status);
			if (Func.isNotBlank(posterUrl)) wrapper.set(TrainingLessonEntity::getPosterUrl, posterUrl);
			if (durationSeconds != null && durationSeconds > 0) wrapper.set(TrainingLessonEntity::getDurationSeconds, durationSeconds);
			lessonMapper.update(null, wrapper);
		} catch (Exception exception) {
			log.error("回写培训课时媒体状态失败，lessonId={}，status={}", lessonId, status, exception);
		}
	}

	private Thread consumeOutput(Process process, StringBuilder output, String threadName) {
		Thread thread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) output.append(line).append('\n');
			} catch (Exception exception) {
				log.debug("读取媒体处理进程输出结束，threadName={}", threadName, exception);
			}
		}, threadName);
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private void joinQuietly(Thread thread) {
		if (thread == null) return;
		try {
			thread.join(3000);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}

	private String limitOutput(CharSequence output) {
		if (output == null || output.length() == 0) return "";
		String value = output.toString();
		return value.length() > MAX_PROCESS_OUTPUT_LENGTH
			? value.substring(value.length() - MAX_PROCESS_OUTPUT_LENGTH)
			: value;
	}
}
