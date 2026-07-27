package org.springblade.modules.pointsbehavior.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BehaviorEventCode {

	DAILY_SIGNIN_SUCCESS("DAILY_SIGNIN_SUCCESS", "每日签到成功"),
	SIGNIN_STREAK_7_SUCCESS("SIGNIN_STREAK_7_SUCCESS", "连续签到7天达成"),
	SIGNIN_STREAK_30_SUCCESS("SIGNIN_STREAK_30_SUCCESS", "连续签到30天达成"),
	INVITE_PUBLISH_SUCCESS("INVITE_PUBLISH_SUCCESS", "发布绿动有约成功"),
	INVITE_APPLY_SUCCESS("INVITE_APPLY_SUCCESS", "报名绿动有约成功"),
	INVITE_APPLY_APPROVED("INVITE_APPLY_APPROVED", "绿动有约报名审核通过"),
	CONTENT_PUBLISH_SUCCESS("CONTENT_PUBLISH_SUCCESS", "发布作品成功"),
	CONTENT_COMMENT_SUCCESS("CONTENT_COMMENT_SUCCESS", "评论成功"),
	CONTENT_LIKE_SUCCESS("CONTENT_LIKE_SUCCESS", "点赞成功"),
	CONTENT_BROWSE_SUCCESS("CONTENT_BROWSE_SUCCESS", "浏览内容成功"),
	PROFILE_INTEREST_COMPLETED("PROFILE_INTEREST_COMPLETED", "完成运动爱好选择");

	private final String code;
	private final String desc;
}
