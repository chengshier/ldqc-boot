package org.springblade.modules.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.modules.search.pojo.dto.SearchRecordDTO;
import org.springblade.modules.search.service.ISearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/platform/searchRecord")
@Tag(name = "搜索记录(Platform)", description = "平台搜索记录接口")
public class PlatformSearchRecordController extends BladeController {

    private final ISearchService searchService;

    @GetMapping("/getAllSearchRecord")
    @Operation(summary = "得到当前用户所有的搜索记录")
    public R<List<String>> getAllSearchRecord(@RequestParam String uid) {
        return R.data(searchService.getAllSearchRecord(uid));
    }

    @PostMapping("/addSearchRecord")
    @Operation(summary = "增加搜索记录")
    public R<Void> addSearchRecord(@RequestBody SearchRecordDTO searchRecordDTO) {
        searchService.addSearchRecord(searchRecordDTO);
        return R.status(true);
    }

    @PostMapping("/deleteSearchRecord")
    @Operation(summary = "删除搜索记录")
    public R<Void> deleteSearchRecord(@RequestBody List<String> words, @RequestParam String uid) {
        searchService.deleteSearchRecord(words, uid);
        return R.status(true);
    }
}