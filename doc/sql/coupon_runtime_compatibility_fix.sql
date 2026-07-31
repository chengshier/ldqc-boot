-- 优惠券运行时兼容修复
-- 适用：MySQL 5.7
-- 解决：
-- 1. user_coupon.status 中存放 UNUSED/USED 等字符串，Java 继承字段按整数读取导致转换异常；
-- 2. 历史模板 valid_type=FIXED 但未配置 valid_end_at，领取时报“有效期配置错误”；
-- 3. 历史库缺少 coupon_status 或显式领取规则字段。
-- 执行前请备份 coupon_template、user_coupon、coupon_receive_log。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = p_table
           AND COLUMN_NAME = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 模板字段兼容。
CALL add_column_if_missing('coupon_template', 'valid_type', 'varchar(16) NOT NULL DEFAULT ''RELATIVE'' COMMENT ''FIXED/RELATIVE''');
CALL add_column_if_missing('coupon_template', 'valid_start_at', 'datetime DEFAULT NULL COMMENT ''固定有效期开始''');
CALL add_column_if_missing('coupon_template', 'valid_end_at', 'datetime DEFAULT NULL COMMENT ''固定有效期结束''');
CALL add_column_if_missing('coupon_template', 'valid_days', 'int DEFAULT 30 COMMENT ''领取后有效天数''');
CALL add_column_if_missing('coupon_template', 'receive_start_at', 'datetime DEFAULT NULL COMMENT ''可领取开始时间''');
CALL add_column_if_missing('coupon_template', 'receive_end_at', 'datetime DEFAULT NULL COMMENT ''可领取结束时间''');
CALL add_column_if_missing('coupon_template', 'auth_required', 'tinyint NOT NULL DEFAULT 0 COMMENT ''是否要求认证''');

-- 用户券字段兼容。status 是 TenantEntity 的数值启停字段；券业务状态必须放 coupon_status。
CALL add_column_if_missing('user_coupon', 'status', 'tinyint NOT NULL DEFAULT 1 COMMENT ''数据状态 1正常0停用''');
CALL add_column_if_missing('user_coupon', 'coupon_status', 'varchar(24) NOT NULL DEFAULT ''UNUSED'' COMMENT ''UNUSED/LOCKED/PARTIAL_USED/USED/EXPIRED/INVALID''');
CALL add_column_if_missing('user_coupon', 'valid_start_at', 'datetime DEFAULT NULL COMMENT ''有效期开始''');
CALL add_column_if_missing('user_coupon', 'valid_end_at', 'datetime DEFAULT NULL COMMENT ''有效期结束''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

-- 若历史库把业务券状态错误存进 status，先迁移到 coupon_status。
UPDATE user_coupon
   SET coupon_status = UPPER(CAST(status AS CHAR))
 WHERE UPPER(CAST(status AS CHAR)) IN ('UNUSED','LOCKED','PARTIAL_USED','USED','EXPIRED','INVALID')
   AND (coupon_status IS NULL OR coupon_status = '' OR coupon_status = 'UNUSED');

UPDATE user_coupon
   SET coupon_status = 'UNUSED'
 WHERE coupon_status IS NULL
    OR coupon_status = ''
    OR UPPER(coupon_status) NOT IN ('UNUSED','LOCKED','PARTIAL_USED','USED','EXPIRED','INVALID');

UPDATE user_coupon
   SET coupon_status = UPPER(coupon_status);

-- status 只保留数据启停语义，避免 JDBC 将 UNUSED 当成整数读取。
UPDATE user_coupon
   SET status = '1'
 WHERE status IS NULL
    OR CAST(status AS CHAR) <> '0';

ALTER TABLE user_coupon
    MODIFY COLUMN status tinyint NOT NULL DEFAULT 1 COMMENT '数据状态 1正常0停用',
    MODIFY COLUMN coupon_status varchar(24) NOT NULL DEFAULT 'UNUSED' COMMENT 'UNUSED/LOCKED/PARTIAL_USED/USED/EXPIRED/INVALID';

-- 修复历史模板的有效期配置。
UPDATE coupon_template
   SET valid_type = CASE
       WHEN valid_end_at IS NOT NULL THEN 'FIXED'
       ELSE 'RELATIVE'
   END
 WHERE valid_type IS NULL
    OR valid_type = ''
    OR UPPER(valid_type) NOT IN ('FIXED','RELATIVE');

-- FIXED 却没有结束时间无法确定期限，按历史兼容策略转为领取后有效，默认 30 天。
UPDATE coupon_template
   SET valid_type = 'RELATIVE',
       valid_days = CASE WHEN valid_days IS NULL OR valid_days <= 0 THEN 30 ELSE valid_days END,
       valid_start_at = NULL
 WHERE UPPER(valid_type) = 'FIXED'
   AND valid_end_at IS NULL;

UPDATE coupon_template
   SET valid_days = 30
 WHERE UPPER(valid_type) = 'RELATIVE'
   AND (valid_days IS NULL OR valid_days <= 0);

-- 固定有效期开始时间不能晚于或等于结束时间；此类历史错误清空开始时间，领取时从当前时间起算到既有结束时间。
UPDATE coupon_template
   SET valid_start_at = NULL
 WHERE UPPER(valid_type) = 'FIXED'
   AND valid_start_at IS NOT NULL
   AND valid_end_at IS NOT NULL
   AND valid_start_at >= valid_end_at;

UPDATE coupon_template
   SET valid_type = UPPER(valid_type);

-- 索引兼容。
DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE add_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = p_table
           AND INDEX_NAME = p_index
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_index_if_missing('user_coupon', 'idx_user_coupon_owner_status', 'KEY `idx_user_coupon_owner_status` (`user_id`,`coupon_status`,`is_deleted`,`create_time`)');
CALL add_index_if_missing('coupon_template', 'idx_coupon_validity', 'KEY `idx_coupon_validity` (`status`,`valid_type`,`valid_end_at`,`receive_end_at`,`is_deleted`)');

DROP PROCEDURE IF EXISTS add_index_if_missing;

-- 执行后验证：
-- SHOW COLUMNS FROM user_coupon LIKE 'status';
-- SHOW COLUMNS FROM user_coupon LIKE 'coupon_status';
-- SELECT status, coupon_status, COUNT(*) FROM user_coupon GROUP BY status, coupon_status;
-- SELECT id, coupon_name, valid_type, valid_start_at, valid_end_at, valid_days
--   FROM coupon_template WHERE is_deleted = 0;
