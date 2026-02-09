package org.springblade.modules.search.controller;

import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.search.pojo.dto.ImgDetailSearchDTO;
import org.springblade.modules.search.service.ISearchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@RestController
@RestController("moudleSearchController")
@AllArgsConstructor
@RequestMapping("/blade-search/search")
public class SearchController {

    private final ISearchService searchService;

    @PostMapping("/img")
    public R<List<ImgDetailDTO>> esSearch(@RequestBody ImgDetailSearchDTO searchDTO) {
        return R.data(searchService.esSearch(searchDTO));
    }
}
