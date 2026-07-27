-- 优惠券领取规则与幂等约束升级
-- 适用：MySQL 5.7
-- 执行前请备份 coupon_template、coupon_receive_log、user_coupon。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_missing('coupon_template', 'auth_required', 'tinyint NOT NULL DEFAULT 0 COMMENT ''是否要求认证 0否1是''');
CALL add_column_if_missing('coupon_template', 'receive_start_at', 'datetime DEFAULT NULL COMMENT ''可领取开始时间''');
CALL add_column_if_missing('coupon_template', 'receive_end_at', 'datetime DEFAULT NULL COMMENT ''可领取结束时间''');
DROP PROCEDURE IF EXISTS add_column_if_missing;

-- 将旧 ext_json 中明确要求认证的模板迁移为显式字段。
UPDATE coupon_template
   SET auth_required = 1
 WHERE is_deleted = 0
   AND ext_json IS NOT NULL
   AND JSON_VALID(ext_json) = 1
   AND JSON_EXTRACT(ext_json, '$.receive_auth_required') = TRUE;

UPDATE coupon_template
   SET per_user_limit = CASE WHEN per_user_limit IS NULL OR per_user_limit <= 0 THEN 1 ELSE per_user_limit END,
       min_growth_level = GREATEST(IFNULL(min_growth_level, 0), 0),
       cost_points = CASE WHEN UPPER(IFNULL(acquire_type, 'FREE')) = 'POINTS_EXCHANGE' THEN GREATEST(IFNULL(cost_points,0),0) ELSE 0 END;

-- 清理历史重复成功领取日志，仅保留同一用户同一请求号最早的一条有效记录。
UPDATE coupon_receive_log duplicate_row
JOIN coupon_receive_log keep_row
  ON keep_row.user_id = duplicate_row.user_id
 AND keep_row.request_id = duplicate_row.request_id
 AND keep_row.is_deleted = 0
 AND duplicate_row.is_deleted = 0
 AND keep_row.id < duplicate_row.id
   SET duplicate_row.is_deleted = 1;

DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE add_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD ', p_definition);
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_index_if_missing('coupon_receive_log', 'uk_coupon_receive_user_request', 'UNIQUE KEY `uk_coupon_receive_user_request` (`user_id`,`request_id`,`is_deleted`)');
CALL add_index_if_missing('coupon_receive_log', 'idx_coupon_receive_user_template', 'KEY `idx_coupon_receive_user_template` (`user_id`,`coupon_template_id`,`status`,`is_deleted`)');
CALL add_index_if_missing('coupon_template', 'idx_coupon_receive_window', 'KEY `idx_coupon_receive_window` (`status`,`receive_start_at`,`receive_end_at`,`remain_stock`)');
CALL add_index_if_missing('user_coupon', 'idx_user_coupon_receive_limit', 'KEY `idx_user_coupon_receive_limit` (`user_id`,`coupon_template_id`,`is_deleted`)');
DROP PROCEDURE IF EXISTS add_index_if_missing;
