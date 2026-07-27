package org.springblade.modules.mediaUtil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 视频封面异步线程池配置。
 *
 * 说明：
 * 1. 视频截帧属于 CPU + IO 操作，不建议并发过高。
 * 2. 当前配置适合中小型业务：最多 2 个任务同时处理。
 */
@Configuration
@EnableAsync
public class VideoCoverAsyncConfig {

	@Bean("videoCoverExecutor")
	public Executor videoCoverExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("video-cover-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
}
