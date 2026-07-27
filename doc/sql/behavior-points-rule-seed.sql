-- 绿豆统一事件规则初始化脚本
-- 适用数据库：MySQL 5.7
-- 说明：将已有规则切到新的事件编码，并补充邀约类规则示例

SET NAMES utf8mb4;

-- 1. 把已存在的规则切换到新的事件编码
UPDATE `points_rule` SET `scene_type` = 'DAILY_SIGNIN_SUCCESS', `update_time` = NOW() WHERE `rule_code` = 'DAILY_SIGNIN' AND `is_deleted` = 0;
UPDATE `points_rule` SET `scene_type` = 'SIGNIN_STREAK_7_SUCCESS', `update_time` = NOW() WHERE `rule_code` = 'SIGNIN_STREAK_7' AND `is_deleted` = 0;
UPDATE `points_rule` SET `scene_type` = 'SIGNIN_STREAK_30_SUCCESS', `update_time` = NOW() WHERE `rule_code` = 'SIGNIN_STREAK_30' AND `is_deleted` = 0;
UPDATE `points_rule` SET `scene_type` = 'CONTENT_BROWSE_SUCCESS', `update_time` = NOW() WHERE `rule_code` = 'CONTENT_BROWSE' AND `is_deleted` = 0;
UPDATE `points_rule` SET `scene_type` = 'CONTENT_LIKE_SUCCESS', `update_time` = NOW() WHERE `rule_code` = 'CONTENT_LIKE' AND `is_deleted` = 0;
UPDATE `points_rule` SET `scene_type` = 'CONTENT_COMMENT_SUCCESS', `update_time` = NOW() WHERE `rule_code` = 'CONTENT_COMMENT' AND `is_deleted` = 0;

-- 2. 浏览规则建议增加浏览时长门槛，避免一进一出也发豆
INSERT INTO `points_rule_condition`
(`id`, `rule_code`, `condition_group`, `condition_key`, `condition_op`, `condition_value`, `sort`, `status`, `remark`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `is_deleted`, `tenant_id`)
SELECT 910001, 'CONTENT_BROWSE', 1, 'browseDuration', 'gte', '5', 10, 1, '浏览时长不少于5秒才奖励', 1, 1, NOW(), 1, NOW(), 0, '000000'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `points_rule_condition` WHERE `rule_code` = 'CONTENT_BROWSE' AND `condition_key` = 'browseDuration' AND `is_deleted` = 0);

-- 3. 新增绿动有约相关规则示例
INSERT INTO `points_rule`
(`id`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `is_deleted`, `rule_code`, `rule_name`, `scene_type`, `grant_points`, `daily_limit_count`, `daily_limit_points`, `lifecycle_limit_count`, `require_first_flag`, `status`, `ext_json`, `tenant_id`)
SELECT 910101, 1, 1, NOW(), 1, NOW(), 0, 'INVITE_PUBLISH_BASIC', '发布绿动有约', 'INVITE_PUBLISH_SUCCESS', 2, 3, 6, NULL, 0, 1, NULL, '000000'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `points_rule` WHERE `rule_code` = 'INVITE_PUBLISH_BASIC' AND `is_deleted` = 0);

INSERT INTO `points_rule`
(`id`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `is_deleted`, `rule_code`, `rule_name`, `scene_type`, `grant_points`, `daily_limit_count`, `daily_limit_points`, `lifecycle_limit_count`, `require_first_flag`, `status`, `ext_json`, `tenant_id`)
SELECT 910102, 1, 1, NOW(), 1, NOW(), 0, 'INVITE_APPLY_BASIC', '报名绿动有约', 'INVITE_APPLY_SUCCESS', 2, 3, 6, NULL, 0, 1, NULL, '000000'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `points_rule` WHERE `rule_code` = 'INVITE_APPLY_BASIC' AND `is_deleted` = 0);

INSERT INTO `points_rule`
(`id`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `is_deleted`, `rule_code`, `rule_name`, `scene_type`, `grant_points`, `daily_limit_count`, `daily_limit_points`, `lifecycle_limit_count`, `require_first_flag`, `status`, `ext_json`, `tenant_id`)
SELECT 910103, 1, 1, NOW(), 1, NOW(), 0, 'INVITE_APPLY_APPROVED_BASIC', '绿动有约报名审核通过', 'INVITE_APPLY_APPROVED', 3, 3, 9, NULL, 0, 1, NULL, '000000'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `points_rule` WHERE `rule_code` = 'INVITE_APPLY_APPROVED_BASIC' AND `is_deleted` = 0);

-- 4. 如果你要限制某些项目类型才发绿豆，可以在 points_rule_condition 继续加条件
-- 示例：仅羽毛球和篮球邀约报名发豆
-- INSERT INTO `points_rule_condition`
-- (`id`, `rule_code`, `condition_group`, `condition_key`, `condition_op`, `condition_value`, `sort`, `status`, `remark`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `is_deleted`, `tenant_id`)
-- SELECT 910201, 'INVITE_APPLY_BASIC', 1, 'sportType', 'in', '["BADMINTON","BASKETBALL"]', 10, 1, '仅羽毛球和篮球报名奖励', 1, 1, NOW(), 1, NOW(), 0, '000000'
-- FROM DUAL
-- WHERE NOT EXISTS (SELECT 1 FROM `points_rule_condition` WHERE `rule_code` = 'INVITE_APPLY_BASIC' AND `condition_key` = 'sportType' AND `is_deleted` = 0);
