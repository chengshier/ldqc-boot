package org.springblade.modules.imgDetail.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;

import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mediaUtil.VideoCoverGenerateTool;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.imgDetail.excel.ImgDetailExcel;
import org.springblade.modules.imgDetail.wrapper.ImgDetailWrapper;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

import static io.jsonwebtoken.lang.Strings.hasText;

/**
 * 图片详情表 控制器
 *
 * @author BladeX
 * @since 2026-01-28
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-imgDetail/imgDetail")
@Tag(name = "图片详情表", description = "图片详情表接口")
public class ImgDetailController extends BladeController {

    private final IImgDetailService imgDetailService;

	private final VideoCoverGenerateTool videoCoverGenerateTool;

    // --- BladeX Standard Methods ---

    @GetMapping("/detail")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "详情", description  = "传入imgDetail")
    public R<ImgDetailVO> detail(ImgDetailEntity imgDetail) {
        ImgDetailEntity detail = imgDetailService.getOne(Condition.getQueryWrapper(imgDetail));
        return R.data(ImgDetailWrapper.build().entityVO(detail));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "分页", description  = "传入imgDetail")
    public R<IPage<ImgDetailVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> imgDetail, Query query) {
        IPage<ImgDetailEntity> pages = imgDetailService.page(Condition.getPage(query), Condition.getQueryWrapper(imgDetail, ImgDetailEntity.class));
        return R.data(ImgDetailWrapper.build().pageVO(pages));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "分页", description  = "传入imgDetail")
    public R<IPage<ImgDetailVO>> page(ImgDetailVO imgDetail, Query query) {
        IPage<ImgDetailVO> pages = imgDetailService.selectImgDetailPage(Condition.getPage(query), imgDetail);
        return R.data(pages);
    }

    @PostMapping("/save")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "新增", description  = "传入imgDetail")
    public R save(@Valid @RequestBody ImgDetailEntity imgDetail) {
        return R.status(imgDetailService.save(imgDetail));
    }

    @PostMapping("/update")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "修改", description  = "传入imgDetail")
    public R update(@Valid @RequestBody ImgDetailEntity imgDetail) {
        return R.status(imgDetailService.updateById(imgDetail));
    }

    @PostMapping("/submit")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "新增或修改", description  = "传入imgDetail")
    public R submit(@Valid @RequestBody ImgDetailEntity imgDetail) {

		boolean needGeneratePoster = shouldGenerateVideoPoster(imgDetail);
		boolean success = imgDetailService.saveOrUpdate(imgDetail);

		if (success && needGeneratePoster && imgDetail.getId() != null) {
			ImgDetailEntity taskArticle = new ImgDetailEntity();
			taskArticle.setId(imgDetail.getId());
			taskArticle.setMediaUrl(imgDetail.getMediaUrl());
			taskArticle.setMediaType(imgDetail.getMediaType());
			taskArticle.setPosterUrl(imgDetail.getPosterUrl());
			taskArticle.setCover(imgDetail.getCover());
			videoCoverGenerateTool.generateCoverAsync(taskArticle);
		}
        return R.status(success);
    }

    @PostMapping("/remove")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "逻辑删除", description  = "传入ids")
    public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
        return R.status(imgDetailService.deleteLogic(Func.toLongList(ids)));
    }

    @IsAdmin
    @GetMapping("/export-imgDetail")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "导出数据", description  = "传入imgDetail")
    public void exportImgDetail(@Parameter(hidden = true) @RequestParam Map<String, Object> imgDetail, BladeUser bladeUser, HttpServletResponse response) {
        QueryWrapper<ImgDetailEntity> queryWrapper = Condition.getQueryWrapper(imgDetail, ImgDetailEntity.class);
        List<ImgDetailExcel> list = imgDetailService.exportImgDetail(queryWrapper);
        ExcelUtil.export(response, "图片详情表数据" + DateUtil.time(), "图片详情表数据表", list, ImgDetailExcel.class);
    }

    @PostMapping("/updateStatus")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "更新图片状态", description = "传入id, status")
    public R updateStatus(@RequestParam String id, @RequestParam Integer status) {
        imgDetailService.updateStatus(id, status);
        return R.status(true);
    }

    // --- Yanhuo Methods ---

    @GetMapping("/getOne")
    @ApiOperationSupport(order = 11)
    @Operation(summary = "获取图片信息", description = "传入id")
    public R<ImgDetailVO> getOne(@RequestParam String id) {
        return R.data(imgDetailService.getImgDetail(id));
    }


	@GetMapping("/getAllImgByAlbum")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "获取图片信息", description = "传入id")
	public R<IPage<ImgDetailVO>> getAllImgByAlbum(@RequestParam long page, @RequestParam long limit, @RequestParam String albumId, @RequestParam Integer type) {
		return R.data(imgDetailService.getAllImgByAlbum(page, limit, albumId, type));
	}

    @PostMapping("/publish")
    @ApiOperationSupport(order = 12)
    @Operation(summary = "发布图片", description = "传入imgDetail")
    public R<Long> publish(@RequestBody ImgDetailDTO imgDetail) {
        Long id = imgDetailService.publish(imgDetail);
        if (id != null && isVideoMedia(imgDetail.getMediaType(), imgDetail.getMediaUrl())
            && !hasUsableImageUrl(imgDetail.getPosterUrl()) && !hasUsableImageUrl(imgDetail.getCover())) {
            ImgDetailEntity taskArticle = new ImgDetailEntity();
            taskArticle.setId(id);
            taskArticle.setMediaType(imgDetail.getMediaType());
            taskArticle.setMediaUrl(imgDetail.getMediaUrl());
            // 发布接口与后台提交接口都使用同一套异步截帧逻辑。
            videoCoverGenerateTool.generateCoverAsync(taskArticle);
        }
        return R.data(id);
    }

    @PostMapping("/deleteImgs")
    @ApiOperationSupport(order = 13)
    @Operation(summary = "删除图片", description = "传入ids")
    public R deleteImgs(@RequestParam String ids, @RequestParam String uid) {
        List<Long> idList = Func.toLongList(ids);
        Long userId = Func.toLong(uid);
        imgDetailService.deleteImgs(idList, userId);
        return R.status(true);
    }

    @GetMapping("/getHot")
    @ApiOperationSupport(order = 14)
    @Operation(summary = "获取热门图片", description = "获取热门图片")
    public R<IPage<ImgDetailVO>> getHot(Query query) {
         IPage<ImgDetailVO> page = Condition.getPage(query);
         return R.data(imgDetailService.searchImgDetail(page.getCurrent(), page.getSize(), "", 1));
    }

	private boolean shouldGenerateVideoPoster(ImgDetailEntity imgDetail) {
		if (imgDetail == null) {
			return false;
		}
		if (!isVideoMedia(imgDetail.getMediaType(), imgDetail.getMediaUrl())) {
			return false;
		}
		if (!hasText(imgDetail.getMediaUrl())) {
			return false;
		}
		return !hasUsableImageUrl(imgDetail.getPosterUrl()) && !hasUsableImageUrl(imgDetail.getCover());
	}

	private boolean isVideoMedia(String mediaType, String mediaUrl) {
		if (hasText(mediaType) && "video".equalsIgnoreCase(mediaType.trim())) {
			return true;
		}
		return looksLikeVideoUrl(mediaUrl);
	}

	private boolean hasUsableImageUrl(String url) {
		return hasText(url) && !looksLikeVideoUrl(url);
	}

	private boolean looksLikeVideoUrl(String url) {
		if (!hasText(url)) {
			return false;
		}
		String lower = url.trim().toLowerCase();
		return lower.contains(".mp4") || lower.contains(".mov") || lower.contains(".avi") || lower.contains(".mkv") || lower.contains(".m4v") || lower.contains(".webm") || lower.contains(".m3u8");
	}
}
