package org.springblade.modules.imgDetail.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.imgDetail.pojo.dto.BrowseRecordDTO;
import org.springblade.modules.imgDetail.excel.ImgDetailExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import java.util.List;

/**
 * 图片详情表 服务类
 *
 * @author BladeX
 * @since 2026-01-28
 */
public interface IImgDetailService extends BaseService<ImgDetailEntity> {

    IPage<ImgDetailVO> getPage(IPage<ImgDetailVO> page);
    ImgDetailVO getImgDetail(String id);
    Long publish(ImgDetailDTO imgDetailDTO);
    List<ImgDetailVO> getAllBrowseRecordByUser(long page, long limit, String uid);
    void addBrowseRecord(BrowseRecordDTO browseRecordDTO);
    void delRecord(String uid, List<String> idList);
    IPage<ImgDetailVO> getAllImgByAlbum(long page, long limit, String albumId, Integer type);
    void deleteImgs(List<Long> idList, Long uid);
    IPage<ImgDetailVO> searchImgDetail(long page, long limit, String keyword, Integer type);

    /**
     * 自定义分页
     */
    IPage<ImgDetailVO> selectImgDetailPage(IPage<ImgDetailVO> page, ImgDetailVO imgDetail);

    /**
     * 导出数据
     */
    List<ImgDetailExcel> exportImgDetail(Wrapper<ImgDetailEntity> queryWrapper);

    /**
     * 更新状态
     */
    void updateStatus(String id, Integer status);

    /**
     * 更新评论数
     */
    void updateCommentCount(String id, int count);
}