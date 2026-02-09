package org.springblade.modules.search.controller;

import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.modules.search.pojo.dto.SearchRecordDTO;
import org.springblade.modules.search.service.ISearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/blade-search/record")
public class SearchRecordController {

    private final ISearchService searchService;

    @GetMapping("/list")
    public R<List<String>> getAllSearchRecord(@RequestParam String uid) {
        return R.data(searchService.getAllSearchRecord(uid));
    }

    @PostMapping("/add")
    public R<Void> addSearchRecord(@RequestBody SearchRecordDTO searchRecordDTO) {
        searchService.addSearchRecord(searchRecordDTO);
        return R.status(true);
    }

    @PostMapping("/delete")
    public R<Void> deleteSearchRecord(@RequestBody List<String> words, @RequestParam String uid) {
        searchService.deleteSearchRecord(words, uid);
        return R.status(true);
    }
}