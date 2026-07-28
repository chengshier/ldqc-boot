-- 评论内容安全审核重试升级脚本
-- 适用：MySQL 5.7
-- 执行前：备份数据库；先在测试库验证。
-- 状态约定：0处理中，1通过，2拒绝，3等待自动重试，4等待人工处理。

CREATE TABLE IF NOT EXISTS `content_audit_task` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `tenant_id` varchar(12) DEFAULT '000000' COMMENT '租户ID',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型：TREND_COMMENT/NEWS_COMMENT',
  `biz_id` bigint(20) NOT NULL COMMENT '业务记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '提交用户ID',
  `open_id` varchar(128) DEFAULT NULL COMMENT '微信OpenID快照',
  `content_snapshot` text COMMENT '送审文本快照',
  `audit_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0处理中 1通过 2拒绝 3重试中 4待人工',
  `provider_trace_id` varchar(128) DEFAULT NULL COMMENT '审核服务追踪号',
  `result_code` varchar(64) DEFAULT NULL COMMENT '审核结果码',
  `result_message` varchar(500) DEFAULT NULL COMMENT '审核结果说明',
  `attempt_count` int(11) NOT NULL DEFAULT '0' COMMENT '已尝试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次自动重试时间',
  `audit_time` datetime DEFAULT NULL COMMENT '最终审核时间',
  `create_user` bigint(20) DEFAULT NULL,
  `create_dept` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_user` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `status` int(11) DEFAULT '1',
  `is_deleted` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_content_audit_biz` (`biz_type`,`biz_id`),
  KEY `idx_content_audit_retry` (`audit_status`,`next_retry_time`,`is_deleted`),
  KEY `idx_content_audit_user` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容安全审核任务';

-- MySQL 5.7 没有 ALTER TABLE ADD COLUMN IF NOT EXISTS，使用 information_schema 动态补列。
SET @db_name = DATABASE();

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='content_audit_task' AND COLUMN_NAME='attempt_count'),
  'SELECT 1',
  'ALTER TABLE content_audit_task ADD COLUMN attempt_count int(11) NOT NULL DEFAULT 0 COMMENT ''已尝试次数'' AFTER result_message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='content_audit_task' AND COLUMN_NAME='next_retry_time'),
  'SELECT 1',
  'ALTER TABLE content_audit_task ADD COLUMN next_retry_time datetime DEFAULT NULL COMMENT ''下次自动重试时间'' AFTER attempt_count'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='content_audit_task' AND COLUMN_NAME='audit_time'),
  'SELECT 1',
  'ALTER TABLE content_audit_task ADD COLUMN audit_time datetime DEFAULT NULL COMMENT ''最终审核时间'' AFTER next_retry_time'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='content_audit_task' AND COLUMN_NAME='result_code'),
  'SELECT 1',
  'ALTER TABLE content_audit_task ADD COLUMN result_code varchar(64) DEFAULT NULL COMMENT ''审核结果码'' AFTER provider_trace_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='content_audit_task' AND COLUMN_NAME='result_message'),
  'SELECT 1',
  'ALTER TABLE content_audit_task ADD COLUMN result_message varchar(500) DEFAULT NULL COMMENT ''审核结果说明'' AFTER result_code'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='content_audit_task' AND INDEX_NAME='idx_content_audit_retry'),
  'SELECT 1',
  'ALTER TABLE content_audit_task ADD INDEX idx_content_audit_retry (audit_status, next_retry_time, is_deleted)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='content_audit_task' AND INDEX_NAME='idx_content_audit_biz'),
  'SELECT 1',
  'ALTER TABLE content_audit_task ADD INDEX idx_content_audit_biz (biz_type, biz_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史异常任务初始化：空尝试次数视为已调用一次；重试时间为空则进入下一轮调度。
UPDATE content_audit_task
SET attempt_count = CASE WHEN attempt_count IS NULL OR attempt_count < 1 THEN 1 ELSE attempt_count END,
    next_retry_time = CASE WHEN audit_status = 3 AND next_retry_time IS NULL THEN NOW() ELSE next_retry_time END,
    update_time = NOW()
WHERE is_deleted = 0
  AND audit_status IN (3, 4);

-- 超过五次仍处于重试状态的历史任务转人工待办。
UPDATE content_audit_task
SET audit_status = 4,
    next_retry_time = NULL,
    result_code = COALESCE(result_code, 'RETRY_EXHAUSTED'),
    result_message = COALESCE(result_message, '自动审核连续失败，等待运营人员处理'),
    update_time = NOW()
WHERE is_deleted = 0
  AND audit_status = 3
  AND attempt_count >= 5;

-- 验证查询
SELECT audit_status, COUNT(*) AS task_count
FROM content_audit_task
WHERE is_deleted = 0
GROUP BY audit_status
ORDER BY audit_status;
