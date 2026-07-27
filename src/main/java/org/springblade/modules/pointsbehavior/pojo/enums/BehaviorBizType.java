package org.springblade.modules.pointsbehavior.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BehaviorBizType {

	SIGNIN("signIn", "签到"),
	SPORT_INVITE("sportInvite", "绿动有约"),
	SPORT_INVITE_APPLY("sportInviteApply", "绿动有约申请"),
	IMG_DETAIL("imgDetail", "作品内容"),
	COMMENT("comment", "评论"),
	ALBUM("album", "专辑"),
	USER_PROFILE("userProfile", "用户资料");

	private final String code;
	private final String desc;
}
