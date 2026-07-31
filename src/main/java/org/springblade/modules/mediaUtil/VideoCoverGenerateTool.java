package org.springblade.modules.mediaUtil;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.oss.model.BladeFile;
import org.springblade.modules.contentaudit.service.DynamicContentAutoAuditService;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.resource.builder.OssBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 社区短视频封面异步生成组件。
 *
 * <p>处理结果会同步写入 media_process_status：PROCESSING、READY、FAILED。
 * 封面 READY 后自动提交微信媒体安全审核，封面处理或审核未完成前视频不会公开。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoCoverGenerateTool {

	private final OssBuilder ossBuilder;
	private final IImgDetailService imgDetailService;
	private final DynamicContentAutoAuditService dynamicContentAutoAuditService;

	@Value("${media.ffmpeg-path:ffmpeg}")
	private String ffmpegPath;

	@Value("${media.temp-dir:/data/tmp/video-cover}")
	private String tempDir;

	@Value("${media.max-video-size:209715200}")
	private long maxVideoSize;

	@Async("videoCoverExecutor")
	public void generateCoverAsync(ImgDetailEntity request) {
		if (request == null || request.getId() == null) {
			log.warn("跳过视频封面生成：内容或内容ID为空");
			return;
		}
		Long contentId = request.getId();
		String videoUrl = request.getMediaUrl();
		if (!hasText(videoUrl)) {
			markFailed(contentId, null, "视频地址为空");
			return;
		}

		markProcessing(contentId, videoUrl);
		Path videoPath = null;
		Path coverPath = null;
		try {
			ImgDetailEntity latest = imgDetailService.getById(contentId);
			if (latest == null || latest.getIsDeleted() != null && latest.getIsDeleted() == 1) {
				log.warn("内容不存在，取消视频封面处理，contentId={}", contentId);
				return;
			}
			if (!videoUrl.equals(latest.getMediaUrl())) {
				log.warn("视频地址已经变化，取消旧任务，contentId={}", contentId);
				return;
			}
			if (hasUsablePoster(latest.getPosterUrl(), latest.getCover())) {
				markReady(contentId, videoUrl, null);
				dynamicContentAutoAuditService.resumeAfterPosterReadyAsync(contentId);
				return;
			}

			String uuid = UUID.randomUUID().toString().replace("-", "");
			Path directory = Paths.get(tempDir);
			Files.createDirectories(directory);
			videoPath = directory.resolve(uuid + resolveVideoExtension(videoUrl));
			coverPath = directory.resolve(uuid + ".jpg");

			downloadVideo(videoUrl, videoPath, maxVideoSize);
			if (!generateCoverByFfmpeg(videoPath, coverPath)) {
				markFailed(contentId, videoUrl, "FFmpeg截取封面失败");
				return;
			}

			String tenantId = hasText(latest.getTenantId()) ? latest.getTenantId() : "000000";
			String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
			String objectName = "video-cover/" + datePath + "/" + uuid + ".jpg";
			BladeFile uploaded;
			try (InputStream stream = Files.newInputStream(coverPath)) {
				uploaded = ossBuilder.template(tenantId, "").putFile(objectName, stream);
			}
			if (uploaded == null || !hasText(uploaded.getLink())) {
				markFailed(contentId, videoUrl, "封面上传后未返回地址");
				return;
			}
			markReady(contentId, videoUrl, uploaded.getLink());
			dynamicContentAutoAuditService.resumeAfterPosterReadyAsync(contentId);
			log.info("视频封面生成并提交自动审核，contentId={}，cover={}", contentId, uploaded.getLink());
		} catch (Exception exception) {
			log.error("视频封面处理异常，contentId={}，videoUrl={}", contentId, videoUrl, exception);
			markFailed(contentId, videoUrl, exception.getMessage());
		} finally {
			deleteQuietly(videoPath);
			deleteQuietly(coverPath);
		}
	}

	private void markProcessing(Long contentId, String videoUrl) {
		LambdaUpdateWrapper<ImgDetailEntity> wrapper = Wrappers.<ImgDetailEntity>lambdaUpdate()
			.eq(ImgDetailEntity::getId, contentId)
			.eq(videoUrl != null, ImgDetailEntity::getMediaUrl, videoUrl)
			.set(ImgDetailEntity::getMediaProcessStatus, "PROCESSING")
			.set(ImgDetailEntity::getUpdateTime, LocalDateTime.now());
		imgDetailService.update(wrapper);
	}

	private void markReady(Long contentId, String videoUrl, String coverUrl) {
		LambdaUpdateWrapper<ImgDetailEntity> wrapper = Wrappers.<ImgDetailEntity>lambdaUpdate()
			.eq(ImgDetailEntity::getId, contentId)
			.eq(videoUrl != null, ImgDetailEntity::getMediaUrl, videoUrl)
			.set(ImgDetailEntity::getMediaProcessStatus, "READY")
			.set(ImgDetailEntity::getUpdateTime, LocalDateTime.now());
		if (hasText(coverUrl)) {
			wrapper.set(ImgDetailEntity::getPosterUrl, coverUrl)
				.set(ImgDetailEntity::getCover, coverUrl);
		}
		imgDetailService.update(wrapper);
	}

	private void markFailed(Long contentId, String videoUrl, String reason) {
		try {
			LambdaUpdateWrapper<ImgDetailEntity> wrapper = Wrappers.<ImgDetailEntity>lambdaUpdate()
				.eq(ImgDetailEntity::getId, contentId)
				.eq(videoUrl != null, ImgDetailEntity::getMediaUrl, videoUrl)
				.set(ImgDetailEntity::getMediaProcessStatus, "FAILED")
				.set(ImgDetailEntity::getAuditReason, "视频封面处理失败，等待运营人员处理：" + safeReason(reason))
				.set(ImgDetailEntity::getUpdateTime, LocalDateTime.now());
			imgDetailService.update(wrapper);
			log.warn("视频封面处理失败，contentId={}，reason={}", contentId, safeReason(reason));
		} catch (Exception exception) {
			log.error("回写视频处理失败状态异常，contentId={}", contentId, exception);
		}
	}

	private void downloadVideo(String videoUrl, Path targetPath, long maxSize) throws Exception {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(videoUrl).openConnection();
			connection.setInstanceFollowRedirects(true);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(90000);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Ldqc-Media-Processor/1.0");
			int code = connection.getResponseCode();
			if (code < 200 || code >= 300) {
				throw new IllegalStateException("视频下载失败，HTTP " + code);
			}
			long declaredSize = connection.getContentLengthLong();
			if (declaredSize > maxSize) {
				throw new IllegalStateException("视频大小超过后端处理限制");
			}
			long total = 0L;
			try (InputStream input = connection.getInputStream();
				 OutputStream output = Files.newOutputStream(targetPath,
					 StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				byte[] buffer = new byte[8192];
				int length;
				while ((length = input.read(buffer)) != -1) {
					total += length;
					if (total > maxSize) {
						throw new IllegalStateException("视频大小超过后端处理限制");
					}
					output.write(buffer, 0, length);
				}
			}
			if (!Files.exists(targetPath) || Files.size(targetPath) == 0) {
				throw new IllegalStateException("下载的视频文件为空");
			}
		} finally {
			if (connection != null) connection.disconnect();
		}
	}

	private boolean generateCoverByFfmpeg(Path videoPath, Path coverPath) {
		Process process = null;
		try {
			ProcessBuilder builder = new ProcessBuilder(
				ffmpegPath, "-y", "-ss", "00:00:01", "-i", videoPath.toAbsolutePath().toString(),
				"-frames:v", "1", "-vf", "scale=720:-2", "-q:v", "3", coverPath.toAbsolutePath().toString());
			builder.redirectErrorStream(true);
			process = builder.start();
			StringBuilder output = new StringBuilder();
			Process currentProcess = process;
			Thread readerThread = new Thread(() -> readProcessOutput(currentProcess, output));
			readerThread.setDaemon(true);
			readerThread.start();
			boolean finished = process.waitFor(45, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				return false;
			}
			readerThread.join(1000);
			if (process.exitValue() != 0) {
				log.error("FFmpeg执行失败，output={}", output);
				return false;
			}
			return Files.exists(coverPath) && Files.size(coverPath) > 0;
		} catch (Exception exception) {
			log.error("执行FFmpeg异常", exception);
			if (process != null) process.destroyForcibly();
			return false;
		}
	}

	private void readProcessOutput(Process process, StringBuilder output) {
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append('\n');
			}
		} catch (Exception ignored) {
			// 进程退出时流关闭属于正常情况。
		}
	}

	private String resolveVideoExtension(String videoUrl) {
		String lower = videoUrl == null ? "" : videoUrl.toLowerCase();
		String[] suffixes = {".mp4", ".mov", ".avi", ".mkv", ".m4v", ".webm", ".mpeg", ".mpg"};
		for (String suffix : suffixes) {
			if (lower.contains(suffix)) return suffix;
		}
		return ".mp4";
	}

	private boolean hasUsablePoster(String posterUrl, String cover) {
		return hasText(posterUrl) && !looksLikeVideo(posterUrl)
			|| hasText(cover) && !looksLikeVideo(cover);
	}

	private boolean looksLikeVideo(String value) {
		String lower = value == null ? "" : value.toLowerCase();
		return lower.contains(".mp4") || lower.contains(".mov") || lower.contains(".avi")
			|| lower.contains(".mkv") || lower.contains(".m4v") || lower.contains(".webm") || lower.contains(".m3u8");
	}

	private String safeReason(String reason) {
		if (!hasText(reason)) return "未知处理异常";
		return reason.length() > 300 ? reason.substring(0, 300) : reason;
	}

	private void deleteQuietly(Path path) {
		if (path == null) return;
		try {
			Files.deleteIfExists(path);
		} catch (Exception exception) {
			log.warn("临时文件删除失败：{}", path, exception);
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
