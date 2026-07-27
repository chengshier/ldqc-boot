package org.springblade.modules.banneritem.mapper;

import org.springblade.modules.banneritem.pojo.entity.BannerItemEntity;
import org.springblade.modules.banneritem.pojo.vo.BannerItemVO;
import org.springblade.modules.banneritem.excel.BannerItemExcel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BannerItemMapper extends BaseMapper<BannerItemEntity> {
	List<BannerItemVO> selectBannerItemPage(IPage page, @Param("bannerItem") BannerItemVO bannerItem);
	List<BannerItemVO> selectActiveByPositionCodes(@Param("codes") List<String> codes, @Param("terminal") String terminal);
	List<BannerItemExcel> exportBannerItem(@Param("ew") Wrapper<BannerItemEntity> queryWrapper);
}
