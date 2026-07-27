package org.springblade.modules.bannerposition.pojo.vo;

import org.springblade.modules.bannerposition.pojo.entity.BannerPositionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class BannerPositionVO extends BannerPositionEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	private Integer itemCount;
}
