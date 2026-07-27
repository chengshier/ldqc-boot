package org.springblade.modules.mediaUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 视频封面临时文件兜底清理任务。
 *
 * 说明：
 * 正常情况下 VideoCoverGenerateTool 的 finally 会删除临时视频和临时封面。
 * 但如果服务重启、进程被杀、FFmpeg 卡死，可能留下残留文件。
 * 该任务每小时清理一次 2 小时前的临时文件。
 */
@Slf4j
@Component
public class VideoCoverTempCleanTask {

	@Value("${media.temp-dir:/data/tmp/video-cover}")
	private String tempDir;

	@Scheduled(cron = "0 0 * * * ?")
	public void cleanVideoCoverTempFiles() {
		Path dir = Paths.get(tempDir);
		if (!Files.exists(dir)) {
			return;
		}

		long expireTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000L;

		try (Stream<Path> paths = Files.list(dir)) {
			paths.filter(Files::isRegularFile)
				.filter(path -> {
					try {
						return Files.getLastModifiedTime(path).toMillis() < expireTime;
					} catch (Exception e) {
						return false;
					}
				})
				.forEach(path -> {
					try {
						Files.deleteIfExists(path);
						log.info("清理视频封面临时文件：{}", path);
					} catch (Exception e) {
						log.warn("清理视频临时文件失败：{}", path, e);
					}
				});
		} catch (Exception e) {
			log.error("清理视频临时目录异常", e);
		}
	}
}
