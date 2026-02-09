package org.springblade.modules.imgDetail.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.imgDetail.pojo.dto.BrowseRecordDTO;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 浏览记录 控制器
 *
 * @author BladeX
 * @since 2026-01-28
 */
@RestController
@AllArgsConstructor
@RequestMapping("/blade-imgDetail/browseRecord")
@Tag(name = "浏览记录", description = "浏览记录接口")
public class BrowseRecordController extends BladeController {

    private final IImgDetailService imgDetailService;

    /**
     * 得到所有的浏览记录根据用户id
     */
    @GetMapping("getAllBrowseRecordByUser/{page}/{limit}")
    @Operation(summary = "得到所有的浏览记录根据用户id", description = "得到所有的浏览记录根据用户id")
    public R<List<ImgDetailVO>> getAllBrowseRecordByUser(@PathVariable long page, @PathVariable long limit, String uid) {
        return R.data(imgDetailService.getAllBrowseRecordByUser(page, limit, uid));
    }

    /**
     * 增加一条浏览记录
     */
    @PostMapping("addBrowseRecord")
    @Operation(summary = "增加一条浏览记录", description = "增加一条浏览记录")
    public R addBrowseRecord(@RequestBody BrowseRecordDTO browseRecordDTO) {
        imgDetailService.addBrowseRecord(browseRecordDTO);
        return R.status(true);
    }

    /**
     * 删除浏览记录
     */
    @PostMapping("delRecord/{uid}")
    @Operation(summary = "删除浏览记录", description = "删除浏览记录")
    public R delRecord(@RequestBody List<String> idList, @PathVariable String uid) {
        imgDetailService.delRecord(uid, idList);
        return R.status(true);
    }
}