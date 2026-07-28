package org.springblade.modules.imgDetail.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.contentaudit.service.DynamicContentAutoAuditService;
import org.springblade.modules.imgDetail.excel.ImgDetailExcel;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.service.ContentPublishWorkflowService;
import org.springblade.modules.imgDetail.service.ContentResubmitService;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.imgDetail.wrapper.ImgDetailWrapper;
import org.springblade.modules.mediaUtil.VideoCoverGenerateTool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static io.jsonwebtoken.lang.Strings.hasText;

/**
 * 社区图文与短视频内容控制器。
 *
 * <p>用户发布后优先进入微信文本和媒体自动审核；全部通过后自动公开。
 * 微信建议复核、接口异常、回调超时或用户举报内容仍由管理端运营人员处理。</p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-imgDetail/imgDetail")
@Tag(name = "社区内容", description = "社区内容发布、自动审核、人工复核、展示和运营接口")
public class ImgDetailController extends BladeController {

	private final IImgDetailService imgDetailService;
	private final ContentPublishWorkflowService contentWorkflowService;
	private final ContentResubmitService contentResubmitService;
	private final VideoCoverGenerateTool videoCoverGenerateTool;
	private final DynamicContentAutoAuditService dynamicContentAutoAuditService;

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "管理端内容详情", description = "运营人员查看完整内容与审核字段")
	public R<ImgDetailVO> detail(ImgDetailEntity imgDetail) {
		ImgDetailEntity detail = imgDetailService.getOne(Condition.getQueryWrapper(imgDetail));
		return R.data(ImgDetailWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端内容分页", description = "运营人员按作者、分类、状态查询内容")
	public R<IPage<ImgDetailVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> imgDetail, Query query) {
		IPage<ImgDetailEntity> pages = imgDetailService.page(
			Condition.getPage(query), Condition.getQueryWrapper(imgDetail, ImgDetailEntity.class));
		return R.data(ImgDetailWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端自定义分页", description = "运营人员查询内容及关联展示字段")
	public R<IPage<ImgDetailVO>> page(ImgDetailVO imgDetail, Query query) {
		return R.data(imgDetailService.selectImgDetailPage(Condition.getPage(query), imgDetail));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "管理端新增内容", description = "平台运营人工新增社区内容")
	public R save(@Valid @RequestBody ImgDetailEntity imgDetail) {
		return R.status(imgDetailService.save(imgDetail));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "管理端修改内容", description = "运营纠错，不能代替用户发布审核流程")
	public R update(@Valid @RequestBody ImgDetailEntity imgDetail) {
		return R.status(imgDetailService.updateById(imgDetail));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "管理端保存内容", description = "平台运营新增或修改内容")
	public R submit(@Valid @RequestBody ImgDetailEntity imgDetail) {
		boolean needGeneratePoster = shouldGenerateVideoPoster(imgDetail);
		boolean success = imgDetailService.saveOrUpdate(imgDetail);
		if (success && needGeneratePoster && imgDetail.getId() != null) {
			videoCoverGenerateTool.generateCoverAsync(imgDetail);
		}
		return R.status(success);
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "管理端删除内容", description = "逻辑删除异常内容")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(imgDetailService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-imgDetail")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "导出内容", description = "按当前筛选条件导出内容")
	public void exportImgDetail(@Parameter(hidden = true) @RequestParam Map<String, Object> imgDetail,
								BladeUser bladeUser,
								HttpServletResponse response) {
		QueryWrapper<ImgDetailEntity> queryWrapper = Condition.getQueryWrapper(imgDetail, ImgDetailEntity.class);
		List<ImgDetailExcel> list = imgDetailService.exportImgDetail(queryWrapper);
		ExcelUtil.export(response, "社区内容" + DateUtil.time(), "社区内容", list, ImgDetailExcel.class);
	}

	@IsAdmin
	@PostMapping("/audit")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "人工复核或下架内容", description = "自动审核异常或微信建议复核时使用；action：PASS、REJECT、OFFLINE")
	public R audit(@RequestBody Map<String, Object> body) {
		Long id = Func.toLong(body.get("id"));
		String action = Func.toStr(body.get("action"), "");
		String reason = Func.toStr(body.get("reason"), "");
		if (id == null) {
			return R.fail("缺少内容ID");
		}
		contentWorkflowService.audit(id, action, reason, AuthUtil.getUserId());
		return R.success("操作成功");
	}

	@GetMapping("/getOne")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "内容详情", description = "未发布内容仅作者本人和管理员可查看")
	public R<ImgDetailVO> getOne(@RequestParam String id) {
		return R.data(contentWorkflowService.getVisibleDetail(id, AuthUtil.getUserId(), AuthUtil.isAdministrator()));
	}

	@PostMapping("/publish")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "发布社区内容", description = "保存后自动进行微信文本、图片或视频封面审核，异常内容转人工")
	public R<Long> publish(@RequestBody ImgDetailDTO request) {
		Long id = contentWorkflowService.submit(request, AuthUtil.getUserId());
		ImgDetailEntity content = imgDetailService.getById(id);
		if (content != null && shouldGenerateVideoPoster(content)) {
			videoCoverGenerateTool.generateCoverAsync(content);
		}
		dynamicContentAutoAuditService.startSubmissionAsync(id);
		return R.data(id);
	}

	@PostMapping("/resubmit")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "修改后重新提交", description = "重新生成当前内容快照的微信自动审核任务")
	public R<Long> resubmit(@RequestBody ImgDetailDTO request) {
		ImgDetailEntity content = contentResubmitService.resubmit(request, AuthUtil.getUserId());
		if (shouldGenerateVideoPoster(content)) {
			videoCoverGenerateTool.generateCoverAsync(content);
		}
		dynamicContentAutoAuditService.startSubmissionAsync(content.getId());
		return R.data(content.getId());
	}

	@PostMapping("/deleteImgs")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "删除我的内容", description = "用户只能删除自己发布的内容，用户ID以后端登录态为准")
	public R deleteImgs(@RequestParam String ids) {
		contentWorkflowService.deleteOwned(Func.toLongList(ids), AuthUtil.getUserId());
		return R.success("删除成功");
	}

	@GetMapping("/my-page")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "我的发布", description = "查看当前用户待审核、已发布、被拒绝和已下架内容")
	public R<IPage<ImgDetailVO>> myPage(@RequestParam(required = false) Integer status, Query query) {
		IPage<ImgDetailEntity> page = imgDetailService.page(Condition.getPage(query),
			Wrappers.<ImgDetailEntity>lambdaQuery()
				.eq(ImgDetailEntity::getUserId, AuthUtil.getUserId())
				.eq(status != null, ImgDetailEntity::getStatus, status)
				.eq(ImgDetailEntity::getIsDeleted, 0)
				.orderByDesc(ImgDetailEntity::getCreateTime));
		return R.data(ImgDetailWrapper.build().pageVO(page));
	}

	@GetMapping("/getHot")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "热门内容", description = "只返回审核通过且已发布的内容")
	public R<IPage<ImgDetailVO>> getHot(Query query) {
		IPage<ImgDetailEntity> page = imgDetailService.page(Condition.getPage(query),
			Wrappers.<ImgDetailEntity>lambdaQuery()
				.eq(ImgDetailEntity::getStatus, ContentPublishWorkflowService.STATUS_PUBLISHED)
				.eq(ImgDetailEntity::getIsDeleted, 0)
				.orderByDesc(ImgDetailEntity::getAgreeCount)
				.orderByDesc(ImgDetailEntity::getPublishTime));
		return R.data(ImgDetailWrapper.build().pageVO(page));
	}

	/** 历史相册接口暂保留兼容，不再作为新发布流程依赖。 */
	@GetMapping("/getAllImgByAlbum")
	@ApiOperationSupport(order = 16)
	public R<IPage<ImgDetailVO>> getAllImgByAlbum(@RequestParam long page,
											 @RequestParam long limit,
											 @RequestParam String albumId,
											 @RequestParam Integer type) {
		return R.data(imgDetailService.getAllImgByAlbum(page, limit, albumId, type));
	}

	@IsAdmin
	@PostMapping("/updateStatus")
	@ApiOperationSupport(order = 17)
	@Operation(summary = "兼容状态更新", description = "历史管理端接口，新页面应调用 audit")
	public R updateStatus(@RequestParam String id, @RequestParam Integer status) {
		imgDetailService.updateStatus(id, status);
		return R.status(true);
	}

	private boolean shouldGenerateVideoPoster(ImgDetailEntity content) {
		if (content == null || !isVideoMedia(content.getMediaType(), content.getMediaUrl())) {
			return false;
		}
		return hasText(content.getMediaUrl())
			&& !hasUsableImageUrl(content.getPosterUrl())
			&& !hasUsableImageUrl(content.getCover());
	}

	private boolean isVideoMedia(String mediaType, String mediaUrl) {
		return (hasText(mediaType) && "video".equalsIgnoreCase(mediaType.trim())) || looksLikeVideoUrl(mediaUrl);
	}

	private boolean hasUsableImageUrl(String url) {
		return hasText(url) && !looksLikeVideoUrl(url);
	}

	private boolean looksLikeVideoUrl(String url) {
		if (!hasText(url)) {
			return false;
		}
		String lower = url.trim().toLowerCase();
		return lower.contains(".mp4") || lower.contains(".mov") || lower.contains(".avi")
			|| lower.contains(".mkv") || lower.contains(".m4v") || lower.contains(".webm") || lower.contains(".m3u8");
	}
}
