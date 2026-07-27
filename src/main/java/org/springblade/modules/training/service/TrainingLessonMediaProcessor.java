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
			return;
		}
		String videoUrl = lesson.getVideoUrl();
		markStatus(lessonId, videoUrl, "PROCESSING", null, null);
		Path coverPath = null;
		try {
			Path directory = Paths.get(tempDir, "training-lesson");
			Files.createDirectories(directory);
			String uuid = UUID.randomUUID().toString().replace("-", "");
			coverPath = directory.resolve(uuid + ".jpg");
			if (!generateCover(videoUrl, coverPath)) {
				markStatus(lessonId, videoUrl, "FAILED", null, null);
				return;
			}

			TrainingLessonEntity latest = lessonMapper.selectById(lessonId);
			if (latest == null || !videoUrl.equals(latest.getVideoUrl())) {
				return;
			}
			String tenantId = Func.isBlank(latest.getTenantId()) ? "000000" : latest.getTenantId();
			String objectName = "training/lesson-cover/"
				+ LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "/" + uuid + ".jpg";
			BladeFile uploaded;
			try (InputStream input = Files.newInputStream(coverPath)) {
				uploaded = ossBuilder.template(tenantId, "").putFile(objectName, input);
			}
			if (uploaded == null || Func.isBlank(uploaded.getLink())) {
				markStatus(lessonId, videoUrl, "FAILED", null, null);
				return;
			}
			Integer duration = probeDuration(videoUrl);
			markStatus(lessonId, videoUrl, "READY", uploaded.getLink(), duration);
		} catch (Exception exception) {
			log.error("培训课时媒体处理失败，lessonId={}", lessonId, exception);
			markStatus(lessonId, videoUrl, "FAILED", null, null);
		} finally {
			if (coverPath != null) {
				try {
					Files.deleteIfExists(coverPath);
				} catch (Exception exception) {
					log.warn("删除培训课时临时封面失败：{}", coverPath, exception);
				}
			}
		}
	}

	private boolean generateCover(String videoUrl, Path coverPath) {
		Process process = null;
		try {
			ProcessBuilder builder = new ProcessBuilder(
				ffmpegPath,
				"-y",
				"-ss", "00:00:01",
				"-i", videoUrl,
				"-frames:v", "1",
				"-vf", "scale=960:-2",
				"-q:v", "3",
				coverPath.toAbsolutePath().toString()
			);
			builder.redirectErrorStream(true);
			process = builder.start();
			drainOutput(process);
			boolean finished = process.waitFor(60, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				return false;
			}
			return process.exitValue() == 0 && Files.exists(coverPath) && Files.size(coverPath) > 0;
		} catch (Exception exception) {
			log.error("FFmpeg生成培训课时封面失败，url={}", videoUrl, exception);
			if (process != null) process.destroyForcibly();
			return false;
		}
	}

	private Integer probeDuration(String videoUrl) {
		Process process = null;
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
			StringBuilder output = readOutput(process);
			boolean finished = process.waitFor(45, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				return null;
			}
			double seconds = Double.parseDouble(output.toString().trim());
			return Math.max(0, (int) Math.round(seconds));
		} catch (Exception exception) {
			log.warn("FFprobe读取课时时长失败，url={}", videoUrl, exception);
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

	private void drainOutput(Process process) {
		Thread thread = new Thread(() -> readOutput(process));
		thread.setDaemon(true);
		thread.start();
	}

	private StringBuilder readOutput(Process process) {
		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) output.append(line).append('\n');
		} catch (Exception ignored) {
			// 进程结束时关闭输出流属于正常情况。
		}
		return output;
	}
}
