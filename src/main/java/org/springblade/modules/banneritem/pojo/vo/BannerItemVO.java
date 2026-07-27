package org.springblade.modules.banneritem.pojo.vo;

import org.springblade.modules.banneritem.pojo.entity.BannerItemEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class BannerItemVO extends BannerItemEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	private String positionCode;
	private String positionName;
}
