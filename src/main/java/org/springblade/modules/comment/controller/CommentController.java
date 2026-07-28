package org.springblade.modules.comment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.comment.excel.CommentExcel;
import org.springblade.modules.comment.pojo.dto.CommentDTO;
import org.springblade.modules.comment.pojo.entity.CommentEntity;
import org.springblade.modules.comment.pojo.vo.CommentVO;
import org.springblade.modules.comment.service.ICommentService;
import org.springblade.modules.comment.wrapper.CommentWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 社区评论公开查询、用户操作和管理端维护接口。 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-comment/comment")
@Tag(name = "社区评论", description = "社区评论查询、发布与运营管理接口")
public class CommentController extends BladeController {

	private final ICommentService commentService;

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "管理端详情")
	public R<CommentVO> detail(CommentEntity comment) {
		CommentEntity detail = commentService.getOne(Condition.getQueryWrapper(comment));
		return R.data(CommentWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端列表")
	public R<IPage<CommentVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> comment, Query query) {
		IPage<CommentEntity> pages = commentService.page(Condition.getPage(query), Condition.getQueryWrapper(comment, CommentEntity.class));
		return R.data(CommentWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端分页")
	public R<IPage<CommentVO>> page(CommentVO comment, Query query) {
		return R.data(commentService.selectCommentPage(Condition.getPage(query), comment));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "管理端新增")
	public R save(@Valid @RequestBody CommentEntity comment) {
		return R.status(commentService.save(comment));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "管理端修改")
	public R update(@Valid @RequestBody CommentEntity comment) {
		return R.status(commentService.updateById(comment));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "管理端新增或修改")
	public R submit(@Valid @RequestBody CommentEntity comment) {
		return R.status(commentService.saveOrUpdate(comment));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "管理端逻辑删除")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(commentService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-comment")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出评论")
	public void exportComment(@Parameter(hidden = true) @RequestParam Map<String, Object> comment, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CommentEntity> queryWrapper = Condition.getQueryWrapper(comment, CommentEntity.class);
		List<CommentExcel> list = commentService.exportComment(queryWrapper);
		ExcelUtil.export(response, "评论表数据" + DateUtil.time(), "评论表数据表", list, CommentExcel.class);
	}

	@RequestMapping("/getAllOneCommentByImgId")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "一级评论分页", description = "只返回审核通过的评论")
	public R<IPage<CommentVO>> getAllOneCommentByImgId(Query query, @RequestParam String mid, @RequestParam String uid) {
		return R.data(commentService.getAllOneCommentByImgId(Condition.getPage(query), mid, uid));
	}

	@RequestMapping("/getComment")
	@ApiOperationSupport(order = 21)
	@Operation(summary = "评论信息")
	public R<CommentEntity> getComment(@RequestParam String id) {
		return R.data(commentService.getById(id));
	}

	@RequestMapping("/addComment")
	@ApiOperationSupport(order = 22)
	@Operation(summary = "发表评论", description = "评论用户以后端登录身份为准")
	public R<CommentVO> addComment(@RequestBody CommentDTO comment) {
		Long currentUserId = AuthUtil.getUserId();
		if (Func.isEmpty(currentUserId) || currentUserId <= 0) throw new ServiceException("请先登录后再发表评论");
		comment.setId(null);
		comment.setUid(currentUserId);
		comment.setCount(0L);
		return R.data(commentService.addComment(comment));
	}

	@RequestMapping("/getAllTwoCommentByOneId")
	@ApiOperationSupport(order = 23)
	@Operation(summary = "二级评论分页")
	public R<IPage<CommentVO>> getAllTwoCommentByOneId(Query query, @RequestParam String id, @RequestParam String uid) {
		return R.data(commentService.getAllTwoCommentByOneId(Condition.getPage(query), id, uid));
	}

	@RequestMapping("/getAllTwoComment")
	@ApiOperationSupport(order = 24)
	@Operation(summary = "二级评论列表")
	public R<List<CommentVO>> getAllTwoComment(@RequestParam String id, @RequestParam String uid) {
		return R.data(commentService.getAllTwoComment(id, uid));
	}

	@RequestMapping("/getAllReplyComment")
	@ApiOperationSupport(order = 25)
	@Operation(summary = "我的评论回复")
	public R<List<CommentVO>> getAllReplyComment(Query query, @RequestParam String uid) {
		return R.data(commentService.getAllReplyComment(Condition.getPage(query), uid));
	}

	@RequestMapping("/getAllComment")
	@ApiOperationSupport(order = 26)
	@Operation(summary = "评论分页", description = "返回一级评论并携带一条二级评论")
	public R<IPage<CommentVO>> getAllComment(Query query, @RequestParam String mid, @RequestParam String uid) {
		return R.data(commentService.getAllComment(Condition.getPage(query), mid, uid));
	}

	@RequestMapping("/scrollComment")
	@ApiOperationSupport(order = 27)
	@Operation(summary = "定位评论")
	public R<Map<String, Object>> scrollComment(@RequestParam String id, @RequestParam String mid, @RequestParam String uid) {
		return R.data(commentService.scrollComment(id, mid, uid));
	}

	@RequestMapping("/delComment")
	@ApiOperationSupport(order = 28)
	@Operation(summary = "删除自己的评论")
	public R<Void> delComment(@RequestParam String id) {
		Long currentUserId = AuthUtil.getUserId();
		CommentEntity comment = commentService.getById(id);
		if (comment == null) return R.status(true);
		if (Func.isEmpty(currentUserId) || !Objects.equals(comment.getUid(), currentUserId)) {
			throw new ServiceException("只能删除自己的评论");
		}
		commentService.delComment(id);
		return R.status(true);
	}

	@RequestMapping("/getAllTrendCommentByImage")
	@ApiOperationSupport(order = 29)
	@Operation(summary = "热门评论")
	public R<IPage<CommentVO>> getAllTrendCommentByImage(Query query, @RequestParam String mid, @RequestParam String uid) {
		return R.data(commentService.getAllTrendCommentByImage(Condition.getPage(query), mid, uid));
	}
}
