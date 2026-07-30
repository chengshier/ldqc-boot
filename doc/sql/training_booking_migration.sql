-- 体育课程线下预约闭环
-- 适用：MySQL 5.7
-- 说明：本表承载线下/混合课程的预约申请与平台确认，不伪造微信支付结果。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ldqc_training_booking` (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(12) NOT NULL DEFAULT '000000' COMMENT '租户ID',
  `booking_no` varchar(64) NOT NULL COMMENT '预约单号',
  `request_id` varchar(64) NOT NULL COMMENT '客户端幂等请求号',
  `user_id` bigint NOT NULL COMMENT '预约用户ID',
  `training_id` bigint NOT NULL COMMENT '课程ID',
  `training_title_snapshot` varchar(200) NOT NULL COMMENT '课程标题快照',
  `cover_image_snapshot` varchar(1000) DEFAULT NULL COMMENT '课程封面快照',
  `content_mode_snapshot` varchar(16) NOT NULL DEFAULT 'OFFLINE' COMMENT 'OFFLINE/MIXED',
  `course_type_snapshot` varchar(100) DEFAULT NULL COMMENT '课程类型快照',
  `price_snapshot` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '课程价格快照，仅展示不代表已支付',
  `location_snapshot` varchar(300) DEFAULT NULL COMMENT '上课地点快照',
  `address_snapshot` varchar(500) DEFAULT NULL COMMENT '详细地址快照',
  `contact_name` varchar(100) NOT NULL COMMENT '联系人',
  `contact_phone` varchar(32) NOT NULL COMMENT '联系电话',
  `participant_count` int NOT NULL DEFAULT 1 COMMENT '参与人数',
  `preferred_time` varchar(100) DEFAULT NULL COMMENT '期望时间说明',
  `remark` varchar(500) DEFAULT NULL COMMENT '预约备注',
  `booking_status` varchar(24) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/CONFIRMED/REJECTED/CANCELLED/COMPLETED',
  `audit_reason` varchar(500) DEFAULT NULL COMMENT '确认或驳回说明',
  `confirmed_at` datetime DEFAULT NULL COMMENT '确认时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `cancelled_at` datetime DEFAULT NULL COMMENT '取消时间',
  `status` int NOT NULL DEFAULT 1 COMMENT '数据状态 1正常0停用',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_booking_user_request` (`user_id`,`request_id`,`is_deleted`),
  UNIQUE KEY `uk_training_booking_no` (`booking_no`,`is_deleted`),
  KEY `idx_training_booking_user_status` (`user_id`,`booking_status`,`create_time`),
  KEY `idx_training_booking_course_status` (`training_id`,`booking_status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体育课程线下预约';

-- 执行后验证
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME = 'ldqc_training_booking'
 ORDER BY ORDINAL_POSITION;
