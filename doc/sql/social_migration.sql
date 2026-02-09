-- ========================================
-- 烟火社交功能数据库迁移脚本
-- 目标: BladeX 4.7.0 单体版本
-- 日期: 2026-01-26
-- ========================================

-- 注意: 烟火的t_user表与BladeX的blade_user表冲突
-- 解决方案: 创建social_user_ext扩展表存储社交特有字段,复用blade_user表

-- ========================================
-- 1. 用户扩展表 (存储社交相关的用户信息)
-- ========================================
DROP TABLE IF EXISTS `social_user_ext`;
CREATE TABLE `social_user_ext` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联blade_user表的id',
  `trend_count` bigint DEFAULT '0' COMMENT '动态数量',
  `follow_count` bigint DEFAULT '0' COMMENT '关注数量',
  `fan_count` bigint DEFAULT '0' COMMENT '粉丝数量',
  `cover` longtext COMMENT '个人主页封面',
  `description` longtext COMMENT '个人简介',
  `birthday` varchar(50) DEFAULT NULL COMMENT '生日',
  `address` varchar(50) DEFAULT NULL COMMENT '地址',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  `status` int DEFAULT '1' COMMENT '状态',
  `is_deleted` int DEFAULT '0' COMMENT '是否已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='社交用户扩展表';

-- ========================================
-- 2. 图片详情表 (核心业务表)
-- ========================================
DROP TABLE IF EXISTS `t_img_detail`;
CREATE TABLE `t_img_detail` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `content` longtext COMMENT '图片信息内容',
  `cover` varchar(255) DEFAULT NULL COMMENT '图片封面',
  `user_id` bigint DEFAULT NULL COMMENT '发布图片信息的用户id',
  `category_id` bigint DEFAULT NULL COMMENT '图片所属的二级分类',
  `category_pid` bigint DEFAULT NULL COMMENT '图片所属的一级分类',
  `imgs_url` longtext COMMENT '图片的地址信息(JSON数组)',
  `count` int DEFAULT NULL COMMENT '图片数量',
  `sort` int DEFAULT NULL COMMENT '排序',
  `status` tinyint DEFAULT NULL COMMENT '图片的状态',
  `view_count` bigint DEFAULT '0' COMMENT '浏览数量',
  `agree_count` bigint DEFAULT '0' COMMENT '点赞数量',
  `collection_count` bigint DEFAULT '0' COMMENT '收藏数量',
  `comment_count` bigint DEFAULT '0' COMMENT '评论数量',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_update_date` (`update_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='图片详情表';

-- ========================================
-- 3. 分类表
-- ========================================
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `pid` bigint DEFAULT NULL COMMENT '分类的父级id',
  `sort` int DEFAULT NULL COMMENT '排序',
  `count` bigint DEFAULT NULL COMMENT '热门的分类',
  `description` longtext COMMENT '描述',
  `cover` varchar(255) DEFAULT NULL COMMENT '分类的封面,如果是一级分类就是随便看看的封面,二级分类则是主封面',
  `hot_cover` varchar(255) DEFAULT NULL COMMENT '热门封面',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表';

-- ========================================
-- 4. 评论表
-- ========================================
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `mid` bigint DEFAULT NULL COMMENT '评论的图片信息id',
  `uid` bigint DEFAULT NULL COMMENT '发布评论的用户id',
  `pid` bigint DEFAULT '0' COMMENT '评论的父id',
  `reply_id` bigint DEFAULT '0' COMMENT '回复某一条评论的id',
  `reply_uid` bigint DEFAULT '0' COMMENT '回复某一条评论的用户id',
  `level` int DEFAULT NULL COMMENT '评论等级',
  `sort` int DEFAULT NULL COMMENT '评论排序',
  `content` longtext COMMENT '评论内容',
  `count` bigint DEFAULT '0' COMMENT '点赞次数',
  `two_nums` bigint DEFAULT '0' COMMENT '二级评论数量',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_mid` (`mid`),
  KEY `idx_uid` (`uid`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- ========================================
-- 5. 点赞收藏表
-- ========================================
DROP TABLE IF EXISTS `t_agree_collect`;
CREATE TABLE `t_agree_collect` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `uid` bigint NOT NULL COMMENT '点赞的用户',
  `agree_collect_id` bigint NOT NULL COMMENT '点赞和收藏的id(可能是图片或者评论)',
  `agree_collect_uid` bigint NOT NULL COMMENT '点赞和收藏通知的用户',
  `type` int NOT NULL COMMENT '类型:1-点赞图片,2-收藏图片,3-点赞评论',
  `creator` bigint NOT NULL COMMENT '创建人',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_agree_collect_id` (`agree_collect_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='点赞收藏表';

-- ========================================
-- 6. 关注表
-- ========================================
DROP TABLE IF EXISTS `t_follow`;
CREATE TABLE `t_follow` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `uid` bigint DEFAULT NULL COMMENT '用户id',
  `fid` bigint DEFAULT NULL COMMENT '关注的用户id',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_fid` (`fid`),
  UNIQUE KEY `uk_uid_fid` (`uid`, `fid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='关注表';

-- ========================================
-- 7. 专辑表
-- ========================================
DROP TABLE IF EXISTS `t_album`;
CREATE TABLE `t_album` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) DEFAULT NULL COMMENT '专辑名称',
  `uid` bigint DEFAULT NULL COMMENT '专辑发布的用户id',
  `cover` varchar(255) DEFAULT NULL COMMENT '专辑封面',
  `sort` int DEFAULT NULL COMMENT '专辑排序',
  `img_count` bigint DEFAULT '0' COMMENT '图片数量',
  `collection_count` bigint DEFAULT '0' COMMENT '收藏数量',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='专辑表';

-- ========================================
-- 8. 专辑图片关系表
-- ========================================
DROP TABLE IF EXISTS `t_album_img_relation`;
CREATE TABLE `t_album_img_relation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `aid` bigint DEFAULT NULL COMMENT '专辑id',
  `mid` bigint DEFAULT NULL COMMENT '图片信息id',
  `sort` int DEFAULT NULL COMMENT '排序',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_aid` (`aid`),
  KEY `idx_mid` (`mid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='专辑图片关系表';

-- ========================================
-- 9. 标签表
-- ========================================
DROP TABLE IF EXISTS `t_tag`;
CREATE TABLE `t_tag` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `sort` int DEFAULT NULL COMMENT '排序',
  `count` bigint DEFAULT NULL COMMENT '数量',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';

-- ========================================
-- 10. 标签图片关系表
-- ========================================
DROP TABLE IF EXISTS `t_tag_img_relation`;
CREATE TABLE `t_tag_img_relation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `mid` bigint NOT NULL COMMENT '图像信息id',
  `tid` bigint NOT NULL COMMENT '标签的id',
  PRIMARY KEY (`id`),
  KEY `idx_mid` (`mid`),
  KEY `idx_tid` (`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签图片关系表';

-- ========================================
-- 11. 消息表
-- ========================================
DROP TABLE IF EXISTS `t_message`;
CREATE TABLE `t_message` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `send_id` bigint DEFAULT NULL COMMENT '发送方的用户id',
  `accept_id` bigint DEFAULT NULL COMMENT '接收方的用户id',
  `content` longtext COMMENT '内容',
  `time` longtext COMMENT '时间',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_send_id` (`send_id`),
  KEY `idx_accept_id` (`accept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息表';

-- ========================================
-- 12. 消息用户关系表
-- ========================================
DROP TABLE IF EXISTS `t_message_user_relation`;
CREATE TABLE `t_message_user_relation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `send_id` bigint DEFAULT NULL COMMENT '发送方的用户id',
  `accept_id` bigint DEFAULT NULL COMMENT '接收方的用户id',
  `count` int DEFAULT '0' COMMENT '未查看的消息数量',
  `content` longtext COMMENT '最后一条的内容',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '最后一条信息的时间',
  PRIMARY KEY (`id`),
  KEY `idx_send_accept` (`send_id`, `accept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息用户关系表';

-- ========================================
-- 13. 第三方登录关系表
-- ========================================
DROP TABLE IF EXISTS `t_user_other_login_relation`;
CREATE TABLE `t_user_other_login_relation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `uid` bigint DEFAULT NULL COMMENT '用户id',
  `other_user_id` varchar(50) DEFAULT NULL COMMENT '第三方用户id',
  `other_username` varchar(100) DEFAULT NULL COMMENT '第三方用户名',
  `other_avatar` varchar(255) DEFAULT NULL COMMENT '第三方头像',
  `other_token` varchar(255) DEFAULT NULL COMMENT '第三方token',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_other_user_id` (`other_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='第三方登录关系表';

-- ========================================
-- 数据迁移说明
-- ========================================
-- 1. 用户数据迁移:
--    - 将yanhuo的t_user表数据迁移到blade_user表
--    - 将社交特有字段(trend_count, follow_count, fan_count等)迁移到social_user_ext表
--
-- 2. 其他表数据:
--    - 直接从yanhuo.sql导入到对应的表中
--
-- 3. 注意事项:
--    - 确保user_id字段正确关联blade_user表的id
--    - 检查所有外键关系是否正确
--    - 更新ElasticSearch索引数据
--    - 更新Redis缓存数据

