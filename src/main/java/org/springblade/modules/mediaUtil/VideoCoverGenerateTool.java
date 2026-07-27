package org.springblade.modules.mediaUtil;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.oss.model.BladeFile;
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
 * 视频封面生成工具组件。
 *
 * 使用场景：
 * 1. 前端先通过 BladeX 原有 /put-file 上传视频，得到 videoUrl。
 * 2. 前端保存文章，coverUrl 为空。
 * 3. 后端保存文章成功后，调用 generateCoverAsync(article)。
 * 4. 本工具异步下载视频、FFmpeg 截图、上传封面、根据文章 id 更新 coverUrl。
 *
 * 注意：
 * - 这是 Spring Bean，不建议写成 static 工具类，因为要注入 OssBuilder、ArticleService 和配置项。
 * - 请把 Article、ArticleService、字段名替换为你项目中的真实类名和字段名。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoCoverGenerateTool {

	private final OssBuilder ossBuilder;

	/**
	 * TODO：替换为你项目里的真实 Service。
	 * 例如：private final IPostService postService;
	 * 或：private final IArticleService articleService;
	 */
	private final IImgDetailService imgDetailService;

	@Value("${media.ffmpeg-path:ffmpeg}")
	private String ffmpegPath;

	@Value("${media.temp-dir:/data/tmp/video-cover}")
	private String tempDir;

	@Value("${media.max-video-size:104857600}")
	private long maxVideoSize;

	/**
	 * 异步生成视频封面。
	 *
	 * @param imgDetail 文章/推文实体，至少需要包含 id 和 videoUrl
	 */
	@Async("videoCoverExecutor")
	public void generateCoverAsync(ImgDetailEntity imgDetail) {
		if (imgDetail == null) {
			log.warn("生成视频封面失败：article 为空");
			return;
		}

		Long articleId = imgDetail.getId();
		String videoUrl = imgDetail.getMediaUrl();

		if (articleId == null) {
			log.warn("生成视频封面失败：articleId 为空");
			return;
		}
		if (!hasText(videoUrl)) {
			log.warn("生成视频封面失败：videoUrl 为空，articleId={}", articleId);
			return;
		}

		Path tempVideoPath = null;
		Path tempCoverPath = null;

		try {
			// 1. 二次查询数据库，防止异步执行期间用户已手动设置封面或修改视频。
			ImgDetailEntity dbArticle = imgDetailService.getById(articleId);
			if (dbArticle == null) {
				log.warn("文章不存在，跳过封面生成，articleId={}", articleId);
				return;
			}
			if (hasUsablePoster(dbArticle.getPosterUrl(), dbArticle.getCover())) {
				log.info("文章已存在可用海报，跳过自动生成，articleId={}", articleId);
				return;
			}
			if (!videoUrl.equals(dbArticle.getMediaUrl())) {
				log.warn("视频地址已变化，跳过封面生成，articleId={}, oldVideoUrl={}, newVideoUrl={}",
					articleId, videoUrl, dbArticle.getMediaUrl());
				return;
			}

			// 2. 准备临时路径。
			String uuid = UUID.randomUUID().toString().replace("-", "");
			String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			Path dir = Paths.get(tempDir);
			Files.createDirectories(dir);

			tempVideoPath = dir.resolve(uuid + resolveVideoExtension(videoUrl));
			tempCoverPath = dir.resolve(uuid + ".jpg");

			// 3. 下载视频到临时目录。
			downloadVideo(videoUrl, tempVideoPath, maxVideoSize);

			// 4. FFmpeg 截取封面。
			boolean success = generateCoverByFfmpeg(tempVideoPath, tempCoverPath);
			if (!success) {
				updateCoverFailed(articleId, videoUrl, "FFmpeg 截取封面失败");
				return;
			}

			// 5. 上传封面到 OSS / MinIO。
			String coverObjectName = "video-cover/" + datePath + "/" + uuid + ".jpg";
//			BladeFile coverFile;
//			try (InputStream coverInputStream = Files.newInputStream(tempCoverPath)) {
//				coverFile = ossBuilder.template().putFile(coverObjectName, coverInputStream);
//			}
			String tenantId = dbArticle.getTenantId();

			if (!hasText(tenantId)) {
				tenantId = "000000";
			}

			BladeFile coverFile;
			try (InputStream coverInputStream = Files.newInputStream(tempCoverPath)) {
				coverFile = ossBuilder.template(tenantId, "").putFile(coverObjectName, coverInputStream);
			}

			// 6. 根据文章 id + videoUrl 更新封面。
			updateCoverSuccess(articleId, videoUrl, coverFile.getLink());
			log.info("视频封面生成成功，articleId={}，coverUrl={}", articleId, coverFile.getLink());
		} catch (Exception e) {
			log.error("生成视频封面异常，articleId={}，videoUrl={}", articleId, videoUrl, e);
			updateCoverFailed(articleId, videoUrl, e.getMessage());
		} finally {
			deleteQuietly(tempVideoPath);
			deleteQuietly(tempCoverPath);
		}
	}

	/**
	 * 下载视频到临时文件。
	 */
	private void downloadVideo(String videoUrl, Path targetPath, long maxSize) throws Exception {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(videoUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(60000);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = connection.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				throw new RuntimeException("下载视频失败，HTTP状态码：" + responseCode);
			}

			long contentLength = connection.getContentLengthLong();
			if (contentLength > maxSize) {
				throw new RuntimeException("视频文件过大，大小=" + contentLength + "，限制=" + maxSize);
			}

			long total = 0L;
			try (
				InputStream inputStream = connection.getInputStream();
				OutputStream outputStream = Files.newOutputStream(
					targetPath,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING
				)
			) {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = inputStream.read(buffer)) != -1) {
					total += len;
					if (total > maxSize) {
						throw new RuntimeException("视频文件超过最大限制：" + maxSize);
					}
					outputStream.write(buffer, 0, len);
				}
			}

			if (!Files.exists(targetPath) || Files.size(targetPath) <= 0) {
				throw new RuntimeException("视频下载完成但文件为空");
			}
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * 使用 FFmpeg 截取封面。
	 */
	private boolean generateCoverByFfmpeg(Path videoPath, Path coverPath) {
		try {
			ProcessBuilder builder = new ProcessBuilder(
				ffmpegPath,
				"-y",
				"-ss", "00:00:01",
				"-i", videoPath.toAbsolutePath().toString(),
				"-frames:v", "1",
				"-vf", "scale=720:-2",
				"-q:v", "3",
				coverPath.toAbsolutePath().toString()
			);

			builder.redirectErrorStream(true);
			Process process = builder.start();

			StringBuilder output = new StringBuilder();
			Thread outputThread = new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
					String line;
					while ((line = reader.readLine()) != null) {
						output.append(line).append("\n");
					}
				} catch (Exception ignored) {
				}
			});
			outputThread.start();

			boolean finished = process.waitFor(30, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				log.error("FFmpeg 截取封面超时，videoPath={}", videoPath);
				return false;
			}

			outputThread.join(1000);
			int exitCode = process.exitValue();
			if (exitCode != 0) {
				log.error("FFmpeg 截取封面失败，exitCode={}，output={}", exitCode, output);
				return false;
			}

			return Files.exists(coverPath) && Files.size(coverPath) > 0;
		} catch (Exception e) {
			log.error("执行 FFmpeg 截封面异常", e);
			return false;
		}
	}

	/**
	 * 成功后根据实体 id 更新封面。
	 */
	private void updateCoverSuccess(Long articleId, String videoUrl, String coverUrl) {
		LambdaUpdateWrapper<ImgDetailEntity> wrapper = Wrappers.lambdaUpdate();
		wrapper.eq(ImgDetailEntity::getId, articleId);
		wrapper.eq(ImgDetailEntity::getMediaUrl, videoUrl);

		// 仅在当前没有可用图片海报时回写，避免覆盖用户手动上传的图片封面。
		wrapper.and(w -> w
			.and(nested -> nested.isNull(ImgDetailEntity::getPosterUrl).or().eq(ImgDetailEntity::getPosterUrl, "").or().like(ImgDetailEntity::getPosterUrl, ".mp4").or().like(ImgDetailEntity::getPosterUrl, ".mov").or().like(ImgDetailEntity::getPosterUrl, ".avi").or().like(ImgDetailEntity::getPosterUrl, ".mkv").or().like(ImgDetailEntity::getPosterUrl, ".m4v").or().like(ImgDetailEntity::getPosterUrl, ".webm").or().like(ImgDetailEntity::getPosterUrl, ".m3u8"))
			.and(nested -> nested.isNull(ImgDetailEntity::getCover).or().eq(ImgDetailEntity::getCover, "").or().like(ImgDetailEntity::getCover, ".mp4").or().like(ImgDetailEntity::getCover, ".mov").or().like(ImgDetailEntity::getCover, ".avi").or().like(ImgDetailEntity::getCover, ".mkv").or().like(ImgDetailEntity::getCover, ".m4v").or().like(ImgDetailEntity::getCover, ".webm").or().like(ImgDetailEntity::getCover, ".m3u8"))
		);

		wrapper.set(ImgDetailEntity::getPosterUrl, coverUrl);
		wrapper.set(ImgDetailEntity::getCover, coverUrl);
//		wrapper.set(ImgDetailEntity::getCoverStatus, "DONE");
		wrapper.set(ImgDetailEntity::getUpdateTime, LocalDateTime.now());

		boolean updated = imgDetailService.update(wrapper);
		if (!updated) {
			log.warn("封面生成成功但文章更新失败，articleId={}，videoUrl={}", articleId, videoUrl);
		}
	}

	/**
	 * 失败后更新状态。
	 */
	private void updateCoverFailed(Long articleId, String videoUrl, String reason) {
		try {
			LambdaUpdateWrapper<ImgDetailEntity> wrapper = Wrappers.lambdaUpdate();
			wrapper.eq(ImgDetailEntity::getId, articleId);
			wrapper.eq(ImgDetailEntity::getMediaUrl, videoUrl);

			// 已经有封面的，不覆盖状态。
			wrapper.and(w -> w.isNull(ImgDetailEntity::getCover).or().eq(ImgDetailEntity::getCover, ""));

//			wrapper.set(ImgDetailEntity::getCoverStatus, "FAILED");
//			wrapper.set(ImgDetailEntity::getCoverFailReason, safeReason(reason));
			wrapper.set(ImgDetailEntity::getUpdateTime, LocalDateTime.now());

			imgDetailService.update(wrapper);
		} catch (Exception e) {
			log.error("更新封面失败状态异常，articleId={}", articleId, e);
		}
	}

	private String resolveVideoExtension(String videoUrl) {
		if (!hasText(videoUrl)) {
			return ".mp4";
		}
		String lower = videoUrl.toLowerCase();
		String[] suffixes = new String[]{".mp4", ".mov", ".avi", ".mkv", ".m4v", ".webm", ".mpeg", ".mpg"};
		for (String suffix : suffixes) {
			if (lower.contains(suffix)) {
				return suffix;
			}
		}
		return ".mp4";
	}

	private boolean looksLikeVideoUrl(String url) {
		if (!hasText(url)) {
			return false;
		}
		String lower = url.trim().toLowerCase();
		return lower.contains(".mp4") || lower.contains(".mov") || lower.contains(".avi") || lower.contains(".mkv") || lower.contains(".m4v") || lower.contains(".webm") || lower.contains(".m3u8");
	}

	private boolean hasUsablePoster(String posterUrl, String cover) {
		return hasText(posterUrl) && !looksLikeVideoUrl(posterUrl)
			|| hasText(cover) && !looksLikeVideoUrl(cover);
	}

	private String safeReason(String reason) {
		if (!hasText(reason)) {
			return "生成视频封面失败";
		}
		return reason.length() > 500 ? reason.substring(0, 500) : reason;
	}

	private void deleteQuietly(Path path) {
		if (path == null) {
			return;
		}
		try {
			Files.deleteIfExists(path);
		} catch (Exception e) {
			log.warn("删除临时文件失败：{}", path, e);
		}
	}

	private boolean hasText(String text) {
		return text != null && text.trim().length() > 0;
	}
}
