package org.springblade.modules.imgDetail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.excel.ImgDetailExcel;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 图片详情表 Mapper 接口
 *
 * @author BladeX
 * @since 2026-01-28
 */
@Mapper
public interface ImgDetailMapper extends BaseMapper<ImgDetailEntity> {

    /**
     * 自定义分页
     *
     * @param page 分页参数
     * @param imgDetail 查询参数
     * @return List<ImgDetailVO>
     */
    List<ImgDetailVO> selectImgDetailPage(IPage page, ImgDetailVO imgDetail);


    /**
     * 获取导出数据
     *
     * @param queryWrapper 查询条件
     * @return List<ImgDetailExcel>
     */
    List<ImgDetailExcel> exportImgDetail(@Param("ew") Wrapper<ImgDetailEntity> queryWrapper);

}