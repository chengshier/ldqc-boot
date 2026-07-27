//package org.springblade.modules.mediaUtil;
//
//import com.baomidou.mybatisplus.core.toolkit.IdWorker;
//import org.springblade.core.tool.api.R;
//import org.springframework.transaction.support.TransactionSynchronization;
//import org.springframework.transaction.support.TransactionSynchronizationManager;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
///**
// * 文章保存接口调用示例。
// *
// * 说明：
// * 1. 这个类不要直接复制覆盖你的 Controller。
// * 2. 重点参考 submit 方法里 needGenerateCover 的判断和 afterCommit 调用。
// * 3. 如果你的保存方法没有事务，也可以在 saveOrUpdate 成功后直接调用 videoCoverGenerateTool.generateCoverAsync(taskArticle)。
// */
//public class ArticleSubmitExample {
//
//	private final ArticleService articleService;
//	private final VideoCoverGenerateTool videoCoverGenerateTool;
//
//	public ArticleSubmitExample(ArticleService articleService,
//								 VideoCoverGenerateTool videoCoverGenerateTool) {
//		this.articleService = articleService;
//		this.videoCoverGenerateTool = videoCoverGenerateTool;
//	}
//
//	@PostMapping("/submit")
//	public R submit(@RequestBody Article article) {
//		boolean needGenerateCover = false;
//
//		// 如果担心 save 后拿不到 id，可以提前生成 id。
//		if (article.getId() == null) {
//			article.setId(IdWorker.getId());
//		}
//
//		if (hasText(article.getVideoUrl()) && !hasText(article.getCoverUrl())) {
//			article.setCoverStatus("GENERATING");
//			needGenerateCover = true;
//		} else if (hasText(article.getCoverUrl())) {
//			article.setCoverStatus("DONE");
//		}
//
//		boolean success = articleService.saveOrUpdate(article);
//
//		if (success && needGenerateCover) {
//			Article taskArticle = new Article();
//			taskArticle.setId(article.getId());
//			taskArticle.setVideoUrl(article.getVideoUrl());
//
//			// 更稳：事务提交后再触发异步任务。
//			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//				@Override
//				public void afterCommit() {
//					videoCoverGenerateTool.generateCoverAsync(taskArticle);
//				}
//			});
//		}
//
//		return R.status(success);
//	}
//
//	private boolean hasText(String text) {
//		return text != null && text.trim().length() > 0;
//	}
//}
