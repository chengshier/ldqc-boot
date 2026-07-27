package org.springblade.modules.banneritem.service.impl;

import org.springblade.modules.banneritem.pojo.entity.BannerItemEntity;
import org.springblade.modules.banneritem.pojo.vo.BannerItemVO;
import org.springblade.modules.banneritem.excel.BannerItemExcel;
import org.springblade.modules.banneritem.mapper.BannerItemMapper;
import org.springblade.modules.banneritem.service.IBannerItemService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

@Service
public class BannerItemServiceImpl extends BaseServiceImpl<BannerItemMapper, BannerItemEntity> implements IBannerItemService {
	@Override
	public IPage<BannerItemVO> selectBannerItemPage(IPage<BannerItemVO> page, BannerItemVO bannerItem) {
		return page.setRecords(baseMapper.selectBannerItemPage(page, bannerItem));
	}

	@Override
	public List<BannerItemVO> listActiveByPositionCodes(List<String> codes, String terminal) {
		return baseMapper.selectActiveByPositionCodes(codes, terminal);
	}

	@Override
	public List<BannerItemExcel> exportBannerItem(Wrapper<BannerItemEntity> queryWrapper) {
		return baseMapper.exportBannerItem(queryWrapper);
	}
}
