package org.springblade.modules.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.platform.PlatformConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.search.pojo.dto.ImgDetailSearchDTO;
import org.springblade.modules.search.pojo.dto.SearchRecordDTO;
import org.springblade.modules.search.service.ISearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

    private final RedisUtils redisUtils;
    private final ElasticsearchClient client;

    private static final String RECORD_INDEX = "search_record";
    private static final String IMG_INDEX = "img_detail";

    @Override
    public Boolean addSearchRecordData(String keyword) {
        Long userId = AuthUtil.getUserId();
        log.info("Add search record: userId={}, keyword={}", userId, keyword);
        try {
            SearchResponse<Map> response = client.search(s -> s
                .index(RECORD_INDEX)
                .query(q -> q
                    .term(t -> t
                        .field("keyword.keyword")
                        .value(keyword)
                    )
                ),
                Map.class
            );

            if (response.hits().total().value() > 0) {
                Hit<Map> hit = response.hits().hits().get(0);
                String id = hit.id();
                Map<String, Object> source = hit.source();
                int count = 1;
                if (source != null && source.containsKey("count")) {
                    count = Integer.parseInt(source.get("count").toString()) + 1;
                }
                source.put("count", count);
                source.put("time", System.currentTimeMillis());

                Map<String, Object> finalSource = source;
                client.update(u -> u
                    .index(RECORD_INDEX)
                    .id(id)
                    .doc(finalSource),
                    Map.class
                );
            } else {
                Map<String, Object> doc = new HashMap<>();
                doc.put("keyword", keyword);
                doc.put("count", 1);
                doc.put("time", System.currentTimeMillis());

                client.index(i -> i
                    .index(RECORD_INDEX)
                    .document(doc)
                );
            }
            return true;
        } catch (Exception e) {
            log.error("ES add record error", e);
            return false;
        }
    }

    @Override
    public List<String> esSearchRecord(String keyword) {
        Long userId = AuthUtil.getUserId();
        log.info("Search record: userId={}, keyword={}", userId, keyword);
        List<String> results = new ArrayList<>();
        try {
            SearchResponse<Map> response = client.search(s -> s
                .index(RECORD_INDEX)
                .query(q -> q
                    .match(m -> m
                        .field("keyword")
                        .query(keyword)
                    )
                )
                .size(10),
                Map.class
            );

            for (Hit<Map> hit : response.hits().hits()) {
                if (hit.source() != null && hit.source().containsKey("keyword")) {
                    results.add(hit.source().get("keyword").toString());
                }
            }
        } catch (Exception e) {
             log.error("ES search record error", e);
        }
        return results;
    }

    @Override
    public List<ImgDetailDTO> esSearch(ImgDetailSearchDTO searchDTO) {
        log.info("ES Search img: keyword={}, type={}, page={}, limit={}", searchDTO.getKeyword(), searchDTO.getType(), searchDTO.getPage(), searchDTO.getLimit());
        List<ImgDetailDTO> results = new ArrayList<>();
        try {
            int page = searchDTO.getPage() != null ? searchDTO.getPage() : 1;
            int limit = searchDTO.getLimit() != null ? searchDTO.getLimit() : 10;
            int from = (page - 1) * limit;

            SearchResponse<ImgDetailDTO> response = client.search(s -> s
                .index(IMG_INDEX)
                .query(q -> {
                    if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                        return q.multiMatch(m -> m
                            .fields("content", "title", "tags", "username")
                            .query(searchDTO.getKeyword())
                        );
                    } else {
                        return q.matchAll(m -> m);
                    }
                })
                .sort(so -> {
                    if (searchDTO.getType() != null) {
                        if (searchDTO.getType() == 1) {
                            return so.field(f -> f.field("agreeCount").order(SortOrder.Desc));
                        } else if (searchDTO.getType() == 2) {
                            return so.field(f -> f.field("createTime").order(SortOrder.Desc));
                        }
                    }
                    return so.score(sc -> sc);
                })
                .from(from)
                .size(limit),
                ImgDetailDTO.class
            );
             for (Hit<ImgDetailDTO> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }
        } catch (Exception e) {
            log.error("ES search img error", e);
        }
        return results;
    }

    @Override
    public List<String> getAllSearchRecord(String uid) {
        String userRecordKey = PlatformConstant.USER_SEARCH_RECORD + uid;
        List<String> objects = redisUtils.lRange(userRecordKey, 0, 20);
        List<String> result = new ArrayList<>();
        if (objects != null) {
            for (Object obj : objects) {
                result.add(String.valueOf(obj));
            }
        }
        return result;
    }

    @Override
    public void addSearchRecord(SearchRecordDTO searchRecordDTO) {
        String userSearchRecordKey = PlatformConstant.USER_SEARCH_RECORD + searchRecordDTO.getUid();
        if (Boolean.TRUE.equals(redisUtils.hasKey(userSearchRecordKey))) {
            redisUtils.lRemove(userSearchRecordKey, 0, searchRecordDTO.getKeyword());
        }
        redisUtils.lLeftPush(userSearchRecordKey, searchRecordDTO.getKeyword());
    }

    @Override
    public void deleteSearchRecord(List<String> words, String uid) {
        String userSearchRecordKey = PlatformConstant.USER_SEARCH_RECORD + uid;
        if (Boolean.TRUE.equals(redisUtils.hasKey(userSearchRecordKey))) {
            for (String word : words) {
                redisUtils.lRemove(userSearchRecordKey, 0, word);
            }
        }
    }
}
