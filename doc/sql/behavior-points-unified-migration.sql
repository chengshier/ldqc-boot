-- 绿豆统一行为事件方案补充表
-- 适用数据库：MySQL 5.7
-- 说明：保留现有 points_account / points_ledger / points_task_log / points_daily_counter / points_rule 体系
-- 本脚本仅新增“行为事件表”和“规则条件表”，并给出推荐索引与示例配置

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_behavior_event` (
  `id` BIGINT(20) NOT NULL COMMENT '主键',
  `event_code` VARCHAR(64) NOT NULL COMMENT '行为事件编码',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型，如sportInvite/imgDetail/comment',
  `biz_id` VARCHAR(64) NOT NULL COMMENT '业务对象ID',
  `event_status` INT(2) NOT NULL DEFAULT '1' COMMENT '事件状态：1成功，0忽略，-1失败',
  `request_id` VARCHAR(128) NOT NULL COMMENT '幂等请求号，同一行为请求全局唯一',
  `source` VARCHAR(32) NOT NULL DEFAULT 'SYSTEM' COMMENT '事件来源：APP/SYSTEM/ADMIN',
  `event_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  `ext_json` TEXT COMMENT '扩展参数JSON，用于规则匹配，如sportType、browseDuration、deviceId',
  `create_user` BIGINT(20) DEFAULT NULL COMMENT '创建人',
  `create_dept` BIGINT(20) DEFAULT NULL COMMENT '创建部门',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` BIGINT(20) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` INT(2) NOT NULL DEFAULT '0' COMMENT '是否已删除',
  `tenant_id` VARCHAR(12) NOT NULL DEFAULT '000000' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_behavior_event_request` (`request_id`),
  KEY `idx_user_behavior_event_user_code_time` (`user_id`, `event_code`, `event_time`),
  KEY `idx_user_behavior_event_biz` (`biz_type`, `biz_id`),
  KEY `idx_user_behavior_event_code_time` (`event_code`, `event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为事件表';

CREATE TABLE IF NOT EXISTS `points_rule_condition` (
  `id` BIGINT(20) NOT NULL COMMENT '主键',
  `rule_code` VARCHAR(64) NOT NULL COMMENT '规则编码，对应points_rule.rule_code',
  `condition_group` INT(11) NOT NULL DEFAULT '1' COMMENT '条件组，同组AND，不同组OR',
  `condition_key` VARCHAR(64) NOT NULL COMMENT '条件字段，如sportType、browseDuration、commentLevel',
  `condition_op` VARCHAR(32) NOT NULL COMMENT '条件操作符，如eq、in、gte、lte、contains',
  `condition_value` VARCHAR(512) DEFAULT NULL COMMENT '条件值，in可存JSON数组字符串',
  `sort` INT(11) NOT NULL DEFAULT '0' COMMENT '排序值，越小越先匹配',
  `status` INT(2) NOT NULL DEFAULT '1' COMMENT '状态：1启用，0停用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '条件说明备注',
  `create_user` BIGINT(20) DEFAULT NULL COMMENT '创建人',
  `create_dept` BIGINT(20) DEFAULT NULL COMMENT '创建部门',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` BIGINT(20) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` INT(2) NOT NULL DEFAULT '0' COMMENT '是否已删除',
  `tenant_id` VARCHAR(12) NOT NULL DEFAULT '000000' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_points_rule_condition_rule` (`rule_code`),
  KEY `idx_points_rule_condition_group_sort` (`rule_code`, `condition_group`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绿豆规则条件表';

-- 推荐：现有规则继续保留，后续新增事件型规则时，scene_type 与 BehaviorEventCode.code 保持一致
-- 下面是可参考的初始化示例，执行前请按你当前库中的ID策略自行调整主键值

-- INSERT INTO points_rule
-- (id, rule_code, rule_name, scene_type, grant_points, daily_limit_count, daily_limit_points, lifecycle_limit_count, require_first_flag, status, ext_json, create_user, create_dept, create_time, update_user, update_time, is_deleted, tenant_id)
-- VALUES
-- (210001, 'RULE_INVITE_APPLY_BASE', '绿动有约报名基础奖励', 'INVITE_APPLY_SUCCESS', 5, 3, 15, NULL, 0, 1, NULL, 1, 1, NOW(), 1, NOW(), 0, '000000');

-- INSERT INTO points_rule_condition
-- (id, rule_code, condition_group, condition_key, condition_op, condition_value, sort, status, remark, create_user, create_dept, create_time, update_user, update_time, is_deleted, tenant_id)
-- VALUES
-- (220001, 'RULE_INVITE_APPLY_BASE', 1, 'sportType', 'in', '["BADMINTON","BASKETBALL"]', 10, 1, '仅羽毛球和篮球报名触发', 1, 1, NOW(), 1, NOW(), 0, '000000');
