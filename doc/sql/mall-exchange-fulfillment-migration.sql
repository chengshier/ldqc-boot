-- 积分商城兑换订单与履约升级
-- 适用数据库：MySQL 5.7
-- 优惠券保持独立业务；商城商品履约类型使用 SHIP/PICKUP/VIRTUAL。
-- 注意：业务订单状态使用 order_status，BladeX 通用 status 只保留数值启停语义。

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

-- 商品运营字段
CALL add_column_if_missing('mall_product', 'gallery_json', 'text DEFAULT NULL COMMENT ''商品图集JSON''');
CALL add_column_if_missing('mall_product', 'category_code', 'varchar(64) DEFAULT NULL COMMENT ''商品分类编码''');
CALL add_column_if_missing('mall_product', 'category_name', 'varchar(100) DEFAULT NULL COMMENT ''商品分类名称快照''');
CALL add_column_if_missing('mall_product', 'spec_json', 'text DEFAULT NULL COMMENT ''可兑换规格JSON''');
CALL add_column_if_missing('mall_product', 'exchange_notice', 'text DEFAULT NULL COMMENT ''兑换与履约说明''');
CALL add_column_if_missing('mall_product', 'sold_qty', 'int NOT NULL DEFAULT 0 COMMENT ''累计兑换数量''');
CALL add_column_if_missing('mall_product', 'fulfillment_type', 'varchar(16) NOT NULL DEFAULT ''SHIP'' COMMENT ''履约类型 SHIP/PICKUP/VIRTUAL''');
CALL add_column_if_missing('mall_product', 'merchant_id', 'bigint DEFAULT NULL COMMENT ''履约商家ID''');
CALL add_column_if_missing('mall_product', 'merchant_name', 'varchar(150) DEFAULT NULL COMMENT ''履约商家名称''');
CALL add_column_if_missing('mall_product', 'pickup_address', 'varchar(500) DEFAULT NULL COMMENT ''到店领取地址''');
CALL add_column_if_missing('mall_product', 'per_user_limit', 'int NOT NULL DEFAULT 0 COMMENT ''每人累计限兑数量，0不限制''');
CALL add_column_if_missing('mall_product', 'max_qty_per_order', 'int NOT NULL DEFAULT 1 COMMENT ''单次最大兑换数量''');
CALL add_column_if_missing('mall_product', 'require_address', 'tinyint NOT NULL DEFAULT 1 COMMENT ''是否需要收货地址''');
CALL add_column_if_missing('mall_product', 'published_at', 'datetime DEFAULT NULL COMMENT ''上架时间''');

-- 订单商品快照、独立业务状态和履约字段
CALL add_column_if_missing('mall_exchange_order', 'order_status', 'varchar(24) NOT NULL DEFAULT ''CREATED'' COMMENT ''CREATED/SUCCESS/FAILED/CANCELLED/COMPLETED''');
CALL add_column_if_missing('mall_exchange_order', 'product_code_snapshot', 'varchar(64) DEFAULT NULL COMMENT ''商品编码快照''');
CALL add_column_if_missing('mall_exchange_order', 'product_name_snapshot', 'varchar(200) DEFAULT NULL COMMENT ''商品名称快照''');
CALL add_column_if_missing('mall_exchange_order', 'cover_url_snapshot', 'varchar(1000) DEFAULT NULL COMMENT ''商品主图快照''');
CALL add_column_if_missing('mall_exchange_order', 'spec_snapshot', 'varchar(500) DEFAULT NULL COMMENT ''规格快照''');
CALL add_column_if_missing('mall_exchange_order', 'unit_points', 'int NOT NULL DEFAULT 0 COMMENT ''单件绿豆快照''');
CALL add_column_if_missing('mall_exchange_order', 'fulfillment_type', 'varchar(16) NOT NULL DEFAULT ''SHIP'' COMMENT ''SHIP/PICKUP/VIRTUAL''');
CALL add_column_if_missing('mall_exchange_order', 'merchant_name_snapshot', 'varchar(150) DEFAULT NULL COMMENT ''履约商家快照''');
CALL add_column_if_missing('mall_exchange_order', 'receiver_name', 'varchar(100) DEFAULT NULL COMMENT ''收货人''');
CALL add_column_if_missing('mall_exchange_order', 'receiver_phone', 'varchar(32) DEFAULT NULL COMMENT ''收货手机号''');
CALL add_column_if_missing('mall_exchange_order', 'receiver_address', 'varchar(500) DEFAULT NULL COMMENT ''收货地址''');
CALL add_column_if_missing('mall_exchange_order', 'pickup_address_snapshot', 'varchar(500) DEFAULT NULL COMMENT ''领取地址快照''');
CALL add_column_if_missing('mall_exchange_order', 'pickup_code', 'varchar(64) DEFAULT NULL COMMENT ''到店领取码''');
CALL add_column_if_missing('mall_exchange_order', 'virtual_content', 'varchar(1000) DEFAULT NULL COMMENT ''虚拟权益内容或兑换码''');
CALL add_column_if_missing('mall_exchange_order', 'logistics_company', 'varchar(100) DEFAULT NULL COMMENT ''物流公司''');
CALL add_column_if_missing('mall_exchange_order', 'logistics_no', 'varchar(100) DEFAULT NULL COMMENT ''物流单号''');
CALL add_column_if_missing('mall_exchange_order', 'fulfillment_status', 'varchar(24) NOT NULL DEFAULT ''PENDING'' COMMENT ''PENDING/PROCESSING/SENT/READY/COMPLETED/CANCELLED''');
CALL add_column_if_missing('mall_exchange_order', 'fulfillment_remark', 'varchar(500) DEFAULT NULL COMMENT ''履约备注''');
CALL add_column_if_missing('mall_exchange_order', 'completed_at', 'datetime DEFAULT NULL COMMENT ''完成时间''');
CALL add_column_if_missing('mall_exchange_order', 'cancelled_at', 'datetime DEFAULT NULL COMMENT ''取消时间''');
CALL add_column_if_missing('mall_exchange_order', 'cancel_reason', 'varchar(500) DEFAULT NULL COMMENT ''取消原因''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_exchange_order' AND INDEX_NAME = 'uk_mall_order_user_request'),
    'SELECT 1',
    'CREATE UNIQUE INDEX uk_mall_order_user_request ON mall_exchange_order(user_id, request_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_exchange_order' AND INDEX_NAME = 'idx_mall_order_user_time'),
    'SELECT 1',
    'CREATE INDEX idx_mall_order_user_time ON mall_exchange_order(user_id, create_time)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_exchange_order' AND INDEX_NAME = 'idx_mall_order_fulfillment'),
    'SELECT 1',
    'CREATE INDEX idx_mall_order_fulfillment ON mall_exchange_order(fulfillment_status, fulfillment_type, create_time)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_exchange_order' AND INDEX_NAME = 'idx_mall_order_business_status'),
    'SELECT 1',
    'CREATE INDEX idx_mall_order_business_status ON mall_exchange_order(order_status, fulfillment_status, is_deleted, create_time)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史订单尽可能补齐商品快照，商品后续改名或下架不影响历史展示。
UPDATE mall_exchange_order o
LEFT JOIN mall_product p ON p.id = o.product_id
   SET o.product_code_snapshot = COALESCE(o.product_code_snapshot, p.product_code),
       o.product_name_snapshot = COALESCE(o.product_name_snapshot, p.product_name, CONCAT('商品', o.product_id)),
       o.cover_url_snapshot = COALESCE(o.cover_url_snapshot, p.cover_url),
       o.unit_points = CASE WHEN o.unit_points = 0 AND o.qty > 0 THEN FLOOR(o.spend_points / o.qty) ELSE o.unit_points END,
       o.fulfillment_type = COALESCE(NULLIF(o.fulfillment_type, ''), p.fulfillment_type, 'SHIP'),
       o.merchant_name_snapshot = COALESCE(o.merchant_name_snapshot, p.merchant_name),
       o.pickup_address_snapshot = COALESCE(o.pickup_address_snapshot, p.pickup_address),
       o.fulfillment_status = CASE
           WHEN o.delivery_status = 'SENT' THEN 'SENT'
           WHEN o.delivery_status = 'FINISHED' THEN 'COMPLETED'
           ELSE COALESCE(NULLIF(o.fulfillment_status, ''), 'PENDING')
       END,
       o.order_status = CASE
           WHEN UPPER(COALESCE(o.fulfillment_status, '')) = 'COMPLETED' OR o.delivery_status = 'FINISHED' THEN 'COMPLETED'
           WHEN UPPER(COALESCE(o.fulfillment_status, '')) = 'CANCELLED' THEN 'CANCELLED'
           WHEN o.fail_reason IS NOT NULL AND TRIM(o.fail_reason) <> '' THEN 'FAILED'
           ELSE COALESCE(NULLIF(UPPER(o.order_status), ''), 'SUCCESS')
       END
 WHERE o.is_deleted = 0;
