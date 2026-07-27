package org.springblade.modules.banneritem.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.banneritem.pojo.entity.BannerItemEntity;
import org.springblade.modules.banneritem.pojo.vo.BannerItemVO;
import org.springblade.modules.banneritem.excel.BannerItemExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import java.util.List;

public interface IBannerItemService extends BaseService<BannerItemEntity> {
	IPage<BannerItemVO> selectBannerItemPage(IPage<BannerItemVO> page, BannerItemVO bannerItem);
	List<BannerItemVO> listActiveByPositionCodes(List<String> codes, String terminal);
	List<BannerItemExcel> exportBannerItem(Wrapper<BannerItemEntity> queryWrapper);
}
