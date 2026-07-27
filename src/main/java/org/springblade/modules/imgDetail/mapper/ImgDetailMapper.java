package org.springblade.modules.imgDetail.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.modules.imgDetail.excel.ImgDetailExcel;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;

import java.util.List;

/**
 * 社区内容 Mapper。
 */
@Mapper
public interface ImgDetailMapper extends BaseMapper<ImgDetailEntity> {

	List<ImgDetailVO> selectImgDetailPage(@Param("page") IPage<?> page,
										 @Param("imgDetail") ImgDetailVO imgDetail);

	List<ImgDetailExcel> exportImgDetail(@Param("ew") Wrapper<ImgDetailEntity> queryWrapper);
}
