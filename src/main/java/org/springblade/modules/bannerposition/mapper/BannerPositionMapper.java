package org.springblade.modules.bannerposition.mapper;

import org.springblade.modules.bannerposition.pojo.entity.BannerPositionEntity;
import org.springblade.modules.bannerposition.pojo.vo.BannerPositionVO;
import org.springblade.modules.bannerposition.excel.BannerPositionExcel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BannerPositionMapper extends BaseMapper<BannerPositionEntity> {
	List<BannerPositionVO> selectBannerPositionPage(IPage page, @Param("bannerPosition") BannerPositionVO bannerPosition);
	List<BannerPositionExcel> exportBannerPosition(@Param("ew") Wrapper<BannerPositionEntity> queryWrapper);
}
