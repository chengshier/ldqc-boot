package org.springblade.modules.search.service;

import org.springblade.modules.search.pojo.dto.ImgDetailSearchDTO;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.search.pojo.dto.SearchRecordDTO;
import java.util.List;

public interface ISearchService {

    /**
     * 添加搜索记录 (ES)
     * @param keyword 关键词
     * @return 搜索结果
     */
    Boolean addSearchRecordData(String keyword);

    /**
     * 获取搜索记录 (ES)
     * @param keyword 关键词
     * @return 搜索记录列表
     */
    List<String> esSearchRecord(String keyword);

    /**
     * ES全文检索图片
     * @param searchDTO 搜索条件
     * @return 图片详情列表
     */
    List<ImgDetailDTO> esSearch(ImgDetailSearchDTO searchDTO);

    /**
     * 获取用户所有搜索记录 (Redis)
     * @param uid 用户ID
     * @return 搜索记录列表
     */
    List<String> getAllSearchRecord(String uid);

    /**
     * 添加搜索记录 (Redis)
     * @param searchRecordDTO 搜索记录DTO
     */
    void addSearchRecord(SearchRecordDTO searchRecordDTO);

    /**
     * 删除搜索记录 (Redis)
     * @param words 关键词列表
     * @param uid 用户ID
     */
    void deleteSearchRecord(List<String> words, String uid);
}