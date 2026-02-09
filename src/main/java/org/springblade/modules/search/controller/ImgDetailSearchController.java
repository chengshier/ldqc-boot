package org.springblade.modules.search.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.search.pojo.dto.ImgDetailSearchDTO;
import org.springblade.modules.search.service.ISearchService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("blade-search/imgDetailSearch")
@Tag(name = "图片搜索", description = "图片搜索接口")
public class ImgDetailSearchController extends BladeController {

    private final ISearchService searchService;

    @PostMapping("/esSearch")
    @Operation(summary = "ES全文检索")
    public R<List<ImgDetailDTO>> esSearch(@RequestBody ImgDetailSearchDTO searchDTO) {
        // TODO: Handle pagination if needed
        return R.data(searchService.esSearch(searchDTO));
    }
}