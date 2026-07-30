-- 积分商城订单业务状态与 BladeX 基础 status 分离修复
-- 适用：MySQL 5.7
-- 解决：历史 MallExchangeOrderEntity 将字符串业务状态映射到 TenantEntity.status，导致类型冲突或查询异常。
-- 执行前请备份 mall_exchange_order。

SET NAMES utf8mb4;

SET @table_exists = (
    SELECT COUNT(1) FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_exchange_order'
);

DROP PROCEDURE IF EXISTS assert_mall_exchange_order_exists;
DELIMITER $$
CREATE PROCEDURE assert_mall_exchange_order_exists()
BEGIN
    IF @table_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'mall_exchange_order 表不存在，请先执行项目基础建表脚本';
    END IF;
END$$
DELIMITER ;
CALL assert_mall_exchange_order_exists();
DROP PROCEDURE IF EXISTS assert_mall_exchange_order_exists;

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

CALL add_column_if_missing(
    'mall_exchange_order',
    'order_status',
    'varchar(24) NOT NULL DEFAULT ''CREATED'' COMMENT ''CREATED/SUCCESS/FAILED/CANCELLED/COMPLETED'''
);
CALL add_column_if_missing('mall_exchange_order', 'status', 'int NOT NULL DEFAULT 1 COMMENT ''数据状态 1正常0停用''');
CALL add_column_if_missing('mall_exchange_order', 'create_dept', 'bigint DEFAULT NULL COMMENT ''创建部门''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

-- 历史 status 若为字符列，先把合法业务状态迁移到 order_status。
SET @status_data_type = (
    SELECT DATA_TYPE FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_exchange_order' AND COLUMN_NAME = 'status'
     LIMIT 1
);

SET @sql = IF(
    @status_data_type IN ('char','varchar','text','tinytext','mediumtext','longtext','enum'),
    'UPDATE mall_exchange_order
        SET order_status = CASE
            WHEN UPPER(TRIM(CAST(status AS CHAR))) = ''INIT'' THEN ''CREATED''
            WHEN UPPER(TRIM(CAST(status AS CHAR))) IN (''CREATED'',''SUCCESS'',''FAILED'',''CANCELLED'',''COMPLETED'')
                THEN UPPER(TRIM(CAST(status AS CHAR)))
            ELSE order_status
        END',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 对没有可靠旧业务状态的记录，根据履约和失败信息回填。
UPDATE mall_exchange_order
   SET order_status = CASE
       WHEN UPPER(COALESCE(fulfillment_status, '')) = 'COMPLETED'
         OR UPPER(COALESCE(delivery_status, '')) = 'FINISHED' THEN 'COMPLETED'
       WHEN UPPER(COALESCE(fulfillment_status, '')) = 'CANCELLED' THEN 'CANCELLED'
       WHEN fail_reason IS NOT NULL AND TRIM(fail_reason) <> '' THEN 'FAILED'
       ELSE 'SUCCESS'
   END
 WHERE order_status IS NULL
    OR TRIM(order_status) = ''
    OR UPPER(order_status) NOT IN ('CREATED','SUCCESS','FAILED','CANCELLED','COMPLETED');

UPDATE mall_exchange_order SET order_status = UPPER(order_status);

-- 恢复 TenantEntity.status 的数值启停语义。
SET @sql = IF(
    @status_data_type IN ('char','varchar','text','tinytext','mediumtext','longtext','enum'),
    'UPDATE mall_exchange_order SET status = ''1'' WHERE status IS NULL OR CAST(status AS CHAR) <> ''0''',
    'UPDATE mall_exchange_order SET status = 1 WHERE status IS NULL'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    @status_data_type IN ('char','varchar','text','tinytext','mediumtext','longtext','enum'),
    'ALTER TABLE mall_exchange_order MODIFY COLUMN status int NOT NULL DEFAULT 1 COMMENT ''数据状态 1正常0停用''',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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

CALL add_index_if_missing(
    'mall_exchange_order',
    'idx_mall_order_business_status',
    'KEY `idx_mall_order_business_status` (`order_status`,`fulfillment_status`,`is_deleted`,`create_time`)'
);

DROP PROCEDURE IF EXISTS add_index_if_missing;

-- 执行后验证
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME = 'mall_exchange_order'
   AND COLUMN_NAME IN ('status','order_status','create_dept')
 ORDER BY ORDINAL_POSITION;

SELECT status, order_status, fulfillment_status, COUNT(*) AS row_count
  FROM mall_exchange_order
 GROUP BY status, order_status, fulfillment_status
 ORDER BY status, order_status, fulfillment_status;
