package org.springblade.modules.training.controller;

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
import org.springblade.modules.training.excel.TrainingExcel;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.pojo.vo.TrainingVO;
import org.springblade.modules.training.service.ITrainingService;
import org.springblade.modules.training.service.TrainingVideoCourseService;
import org.springblade.modules.training.wrapper.TrainingWrapper;
import org.springblade.modules.trainingprogress.pojo.entity.TrainingProgressEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 体育培训课程控制器。
 *
 * <p>通用 CRUD 仅供管理端；小程序使用 mobile-*、lesson-play-token、video-play 和 progress 接口。</p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-training/training")
@Tag(name = "体育培训课程", description = "培训课程运营与长视频学习接口")
public class TrainingController extends BladeController {

	private final ITrainingService trainingService;
	private final TrainingVideoCourseService videoCourseService;

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "管理端课程详情")
	public R<TrainingVO> detail(TrainingEntity training) {
		TrainingEntity detail = trainingService.getOne(Condition.getQueryWrapper(training));
		return R.data(TrainingWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端课程列表")
	public R<IPage<TrainingVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> training, Query query) {
		IPage<TrainingEntity> pages = trainingService.page(
			Condition.getPage(query), Condition.getQueryWrapper(training, TrainingEntity.class));
		return R.data(TrainingWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端课程分页")
	public R<IPage<TrainingVO>> page(TrainingVO training, Query query) {
		return R.data(trainingService.selectTrainingPage(Condition.getPage(query), training));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增课程")
	public R save(@Valid @RequestBody TrainingEntity training) {
		return R.status(trainingService.save(training));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改课程")
	public R update(@Valid @RequestBody TrainingEntity training) {
		return R.status(trainingService.updateById(training));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "保存课程")
	public R submit(@Valid @RequestBody TrainingEntity training) {
		return R.status(trainingService.saveOrUpdate(training));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "删除课程")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(trainingService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-training")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "导出课程")
	public void exportTraining(@Parameter(hidden = true) @RequestParam Map<String, Object> training,
									 BladeUser bladeUser,
									 HttpServletResponse response) {
		QueryWrapper<TrainingEntity> queryWrapper = Condition.getQueryWrapper(training, TrainingEntity.class);
		List<TrainingExcel> list = trainingService.exportTraining(queryWrapper);
		ExcelUtil.export(response, "培训课程" + DateUtil.time(), "培训课程", list, TrainingExcel.class);
	}

	@GetMapping("/mobile-page")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "小程序课程列表", description = "只返回已发布、已启用课程")
	public R<IPage<TrainingVO>> mobilePage(TrainingEntity queryEntity, Query query) {
		IPage<TrainingEntity> page = trainingService.page(Condition.getPage(query),
			Wrappers.<TrainingEntity>lambdaQuery()
				.eq(TrainingEntity::getIsDeleted, 0)
				.eq(TrainingEntity::getStatus, 1)
				.eq(TrainingEntity::getPublishStatus, "PUBLISHED")
				.eq(Func.isNotBlank(queryEntity.getCategory()), TrainingEntity::getCategory, queryEntity.getCategory())
				.eq(Func.isNotBlank(queryEntity.getCourseType()), TrainingEntity::getCourseType, queryEntity.getCourseType())
				.eq(Func.isNotBlank(queryEntity.getContentMode()), TrainingEntity::getContentMode, queryEntity.getContentMode())
				.orderByDesc(TrainingEntity::getSortOrder)
				.orderByDesc(TrainingEntity::getCreateTime));
		return R.data(TrainingWrapper.build().pageVO(page));
	}

	@GetMapping("/mobile-detail")
	@ApiOperationSupport(order = 21)
	@Operation(summary = "小程序课程详情与目录", description = "目录不返回正式视频原始地址")
	public R<Map<String, Object>> mobileDetail(@RequestParam Long id) {
		return R.data(videoCourseService.courseDetail(id, currentUserIdOrNull()));
	}

	@PostMapping("/lesson-play-token")
	@ApiOperationSupport(order = 22)
	@Operation(summary = "获取课时播放令牌", description = "免费、试看或已授权用户可获取五分钟播放链接")
	public R<Map<String, Object>> lessonPlayToken(@RequestBody Map<String, Object> body) {
		Long lessonId = Func.toLong(body.get("lessonId"));
		if (lessonId == null) return R.fail("缺少课时ID");
		return R.data(videoCourseService.createPlayToken(lessonId, currentUserIdOrNull()));
	}

	@GetMapping("/video-play")
	@ApiOperationSupport(order = 23)
	@Operation(summary = "短时播放地址", description = "播放器通过短时令牌跳转到实际视频文件")
	public void videoPlay(@RequestParam String token, HttpServletResponse response) throws IOException {
		String videoUrl = videoCourseService.resolveVideoUrl(token);
		response.setHeader("Cache-Control", "no-store");
		response.sendRedirect(videoUrl);
	}

	@PostMapping("/progress")
	@ApiOperationSupport(order = 24)
	@Operation(summary = "保存学习进度", description = "建议每15至30秒及退出课时时上报")
	public R<Map<String, Object>> saveProgress(@RequestBody Map<String, Object> body) {
		Long lessonId = Func.toLong(body.get("lessonId"));
		Integer progressSeconds = Func.toInt(body.get("progressSeconds"), 0);
		if (lessonId == null) return R.fail("缺少课时ID");
		return R.data(videoCourseService.saveProgress(lessonId, progressSeconds, AuthUtil.getUserId()));
	}

	@GetMapping("/progress")
	@ApiOperationSupport(order = 25)
	@Operation(summary = "课程学习进度")
	public R<List<TrainingProgressEntity>> progress(@RequestParam Long trainingId) {
		return R.data(videoCourseService.listProgress(trainingId, AuthUtil.getUserId()));
	}

	private Long currentUserIdOrNull() {
		Long userId = AuthUtil.getUserId();
		return userId != null && userId > 0 ? userId : null;
	}
}
