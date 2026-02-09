package org.springblade.modules.imgDetail.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import java.util.Objects;

/**
 * 图片详情表 包装类,返回视图层所需的字段
 *
 * @author BladeX
 * @since 2026-01-27
 */
public class ImgDetailWrapper extends BaseEntityWrapper<ImgDetailEntity, ImgDetailVO>  {

public static ImgDetailWrapper build() {
return new ImgDetailWrapper();
}

@Override
public ImgDetailVO entityVO(ImgDetailEntity imgDetail) {
ImgDetailVO imgDetailVO = Objects.requireNonNull(BeanUtil.copyProperties(imgDetail, ImgDetailVO.class));
return imgDetailVO;
}
}