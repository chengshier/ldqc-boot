/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.modules.system.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import org.springblade.core.tool.jackson.Sensitive;
import org.springblade.core.tool.sensitive.SensitiveType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serial;
import java.util.Date;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@TableName("blade_user")
@EqualsAndHashCode(callSuper = true)
public class User extends TenantEntity {

@Serial
private static final long serialVersionUID = 1L;

/**
 * 用户编号
 */
private String code;
/**
 * 用户平台
 */
private Integer userType;
/**
 * 账号
 */
private String account;
/**
 * 密码
 */
private String password;
/**
 * 昵称
 */
private String name;
/**
 * 真名
 */
private String realName;
/**
 * 头像
 */
private String avatar;
/**
 * 邮箱
 */
//@Sensitive(type = SensitiveType.EMAIL)
private String email;
/**
 * 手机
 */
//@Sensitive(type = SensitiveType.MOBILE)
private String phone;
/**
 * 生日
 */
private Date birthday;
/**
 * 性别
 */
private Integer sex;
/**
 * 角色id
 */
private String roleId;
/**
 * 部门id
 */
private String deptId;
/**
 * 岗位id
 */
private String postId;
/**
 * 主管id
 */
private String leaderId;
/**
 * 是否主管
 */
private Integer isLeader;

/**
 * 动态数量
 */
private Long trendCount;

/**
 * 关注数量
 */
private Long followCount;

/**
 * 粉丝数量
 */
private Long fanCount;

/**
 * 获赞数量
 */
@TableField(exist = false)
private Long likeCount;

/**
 * 收藏数量
 */
@TableField(exist = false)
private Long collectCount;

/**
 * 用户简介
	 */
	private String description;

	/**
	 * 是否达人[0:否,1:是]
	 */
	private Integer isTalent;

	/**
	 * 达人排序值,越大越靠前
	 */
	private Integer talentSort;

	/**
	 * 达人标签,逗号分隔
	 */
	private String talentTags;

	/**
	 * 达人简介
	 */
	private String talentIntro;

	/**
	 * 是否在线[0:否,1:是]
	 */
	private Integer talentOnline;
	/**
	 * 认证状态[0:未认证,1:审核中,2:已通过,3:已驳回]
	 */
	private Integer authStatus;

	/**
	 * 主身份编码
	 */
	private String mainIdentityCode;

	/**
	 * 主身份名称
	 */
	private String mainIdentityName;

	/**
	 * 已通过身份,逗号分隔
	 */
	private String identityBadges;

	/**
	 * 最近一次驳回原因
	 */
	private String authRefuseReason;
	/**
	 * 个人封面图
	 */
	private String cover;


	/**
	 * 地址
	 */
	private String address;


}
