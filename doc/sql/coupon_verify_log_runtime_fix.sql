-- 优惠券核销日志运行时字段修复
-- 适用：MySQL 5.7
-- 解决：CouponVerifyLogEntity 继承 TenantEntity，但历史表缺少 verify_status 或 BladeX 基础字段。
-- 执行前请备份 coupon_verify_log。

SET NAMES utf8mb4;

SET @table_exists = (
    SELECT COUNT(1)
      FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'coupon_verify_log'
);

DROP PROCEDURE IF EXISTS assert_coupon_verify_log_exists;
DELIMITER $$
CREATE PROCEDURE assert_coupon_verify_log_exists()
BEGIN
    IF @table_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'coupon_verify_log 表不存在，请先执行项目基础建表脚本';
    END IF;
END$$
DELIMITER ;
CALL assert_coupon_verify_log_exists();
DROP PROCEDURE IF EXISTS assert_coupon_verify_log_exists;

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

CALL add_column_if_missing('coupon_verify_log', 'tenant_id', 'varchar(12) NOT NULL DEFAULT ''000000'' COMMENT ''租户ID''');
CALL add_column_if_missing('coupon_verify_log', 'verify_status', 'varchar(24) NOT NULL DEFAULT ''FINISHED'' COMMENT ''PROCESSING/FINISHED''');
CALL add_column_if_missing('coupon_verify_log', 'create_user', 'bigint DEFAULT NULL COMMENT ''创建人''');
CALL add_column_if_missing('coupon_verify_log', 'create_dept', 'bigint DEFAULT NULL COMMENT ''创建部门''');
CALL add_column_if_missing('coupon_verify_log', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''');
CALL add_column_if_missing('coupon_verify_log', 'update_user', 'bigint DEFAULT NULL COMMENT ''修改人''');
CALL add_column_if_missing('coupon_verify_log', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''修改时间''');
CALL add_column_if_missing('coupon_verify_log', 'status', 'int NOT NULL DEFAULT 1 COMMENT ''数据状态 1正常0停用''');
CALL add_column_if_missing('coupon_verify_log', 'is_deleted', 'int NOT NULL DEFAULT 0 COMMENT ''逻辑删除 0正常1删除''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

UPDATE coupon_verify_log
   SET verify_status = 'FINISHED'
 WHERE verify_status IS NULL
    OR TRIM(verify_status) = '';

UPDATE coupon_verify_log
   SET verify_status = UPPER(verify_status)
 WHERE verify_status IS NOT NULL;

-- 已存在但为 nullable 的历史列统一补值；不强制修改历史库的其他列类型。
UPDATE coupon_verify_log SET tenant_id = '000000' WHERE tenant_id IS NULL OR tenant_id = '';
UPDATE coupon_verify_log SET status = 1 WHERE status IS NULL;
UPDATE coupon_verify_log SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE coupon_verify_log SET create_time = NOW() WHERE create_time IS NULL;
UPDATE coupon_verify_log SET update_time = COALESCE(update_time, create_time, NOW()) WHERE update_time IS NULL;

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

CALL add_index_if_missing(
    'coupon_verify_log',
    'idx_coupon_verify_log_coupon_status',
    'KEY `idx_coupon_verify_log_coupon_status` (`user_coupon_id`,`verify_status`,`is_deleted`,`create_time`)'
);

DROP PROCEDURE IF EXISTS add_index_if_missing;

-- 执行后验证
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME = 'coupon_verify_log'
   AND COLUMN_NAME IN (
       'id','tenant_id','verify_status','create_user','create_dept','create_time',
       'update_user','update_time','status','is_deleted'
   )
 ORDER BY ORDINAL_POSITION;

SELECT verify_status, COUNT(*) AS row_count
  FROM coupon_verify_log
 GROUP BY verify_status
 ORDER BY verify_status;
