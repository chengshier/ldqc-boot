package org.springblade.modules.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.modules.search.service.ISearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/search/searchRecord")
@Tag(name = "搜索记录(ES)", description = "ES搜索记录接口")
public class SearchRecordEsController extends BladeController {

    private final ISearchService searchService;

    @RequestMapping("/addSearchRecordData")
    @Operation(summary = "增加搜索记录(ES)")
    public R<Void> addSearchRecordData(@RequestParam String keyword) {
        searchService.addSearchRecordData(keyword);
        return R.status(true);
    }

    @RequestMapping("/esSearchRecord")
    @Operation(summary = "搜索记录(ES)")
    public R<List<String>> esSearchRecord(@RequestParam String keyword) {
        return R.data(searchService.esSearchRecord(keyword));
    }
}