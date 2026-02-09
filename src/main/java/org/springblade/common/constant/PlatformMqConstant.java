package org.springblade.common.constant;

/**
 * 平台MQ常量
 *
 * @author BladeX
 * @since 2026-01-27
 */
public interface PlatformMqConstant {

    String IMG_DETAIL_STATE_EXCHANGE = "img.detail.state.exchange";

    String IMG_DETAIL_STATE_KEY = "img.detail.state.key";

    String ALBUM_STATE_EXCHANGE = "album.state.exchange";

    String ALBUM_STATE_KEY = "album.state.key";




	String IMG_DETAIL_STATE_QUEUE  = "imgDetailState.queue";


	String USER_STATE_EXCHANGE = "userState.direct";

	String USER_STATE_KEY = "userStateStateKey.update";

	String USER_STATE_QUEUE = "userState.queue";


	String COMMON_STATE_EXCHANGE = "commonState.direct";

	String COMMON_STATE_KEY = "commonStateKey.update";

	String COMMON_STATE_QUEUE = "commonState.queue";


	String ALBUM_STATE_QUEUE = "albumState.queue";
}

