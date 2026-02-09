package org.springblade.modules.system.pojo.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户记录视图类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
public class UserRecordVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long uid;

	/**
	 * 新关注的用户数量
	 */
	private Long addFollowCount = 0L;
	/**
	 * 没有回复的数量
	 */
	private Long noreplyCount = 0L;
	/**
	 * 新点赞和收藏的数量
	 */
	private Long agreeCollectionCount = 0L;

	private Long collectionCount = 0L;
}
