-- 体育课程预约运行时兼容补丁
-- 适用：MySQL 5.7
-- 用于已执行过早期 training_booking_migration.sql 的测试库。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_missing('ldqc_training_booking', 'active_unique_key',
    'varchar(128) DEFAULT NULL COMMENT ''活动预约唯一键 userId:trainingId，终态清空'' AFTER request_id');
CALL add_column_if_missing('ldqc_training_booking', 'create_dept',
    'bigint DEFAULT NULL COMMENT ''创建部门'' AFTER create_user');

DROP PROCEDURE IF EXISTS add_column_if_missing;

-- 只为活动状态回填唯一键；若查询结果存在重复，必须先人工处理，不得直接建立唯一索引。
UPDATE ldqc_training_booking
   SET active_unique_key = CONCAT(user_id, ':', training_id)
 WHERE booking_status IN ('SUBMITTED','CONFIRMED')
   AND is_deleted = 0
   AND (active_unique_key IS NULL OR active_unique_key = '');

SELECT active_unique_key, COUNT(*) AS active_count
  FROM ldqc_training_booking
 WHERE active_unique_key IS NOT NULL
   AND is_deleted = 0
 GROUP BY active_unique_key
HAVING COUNT(*) > 1;

DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE add_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 仅在上面的重复查询返回 0 行后执行本句。
CALL add_index_if_missing('ldqc_training_booking', 'uk_training_booking_active',
    'UNIQUE KEY `uk_training_booking_active` (`active_unique_key`,`is_deleted`)');

DROP PROCEDURE IF EXISTS add_index_if_missing;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME = 'ldqc_training_booking'
   AND COLUMN_NAME IN ('active_unique_key','booking_status','status','create_dept');
