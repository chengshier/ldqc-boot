-- 商城商品运行时字段兼容修复
-- 适用：MySQL 5.7
-- 用途：处理 mall_product 已存在、但历史建表缺少本轮实体字段的环境。
-- 当前报错 Unknown column 'sold_qty' 时可直接执行本脚本。

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

-- MallProductEntity 当前直接查询依赖的商品字段。
CALL add_column_if_missing('mall_product', 'product_code', 'varchar(64) DEFAULT NULL COMMENT ''商品编码''');
CALL add_column_if_missing('mall_product', 'product_name', 'varchar(200) NOT NULL DEFAULT '''' COMMENT ''商品名称''');
CALL add_column_if_missing('mall_product', 'product_desc', 'text DEFAULT NULL COMMENT ''商品说明''');
CALL add_column_if_missing('mall_product', 'product_type', 'varchar(32) NOT NULL DEFAULT ''OTHER'' COMMENT ''EQUIPMENT/DIGITAL/OTHER''');
CALL add_column_if_missing('mall_product', 'cover_url', 'varchar(1000) DEFAULT NULL COMMENT ''商品主图''');
CALL add_column_if_missing('mall_product', 'gallery_json', 'text DEFAULT NULL COMMENT ''商品图集JSON''');
CALL add_column_if_missing('mall_product', 'category_code', 'varchar(64) DEFAULT NULL COMMENT ''商品分类编码''');
CALL add_column_if_missing('mall_product', 'category_name', 'varchar(100) DEFAULT NULL COMMENT ''商品分类名称快照''');
CALL add_column_if_missing('mall_product', 'spec_json', 'text DEFAULT NULL COMMENT ''可兑换规格JSON''');
CALL add_column_if_missing('mall_product', 'exchange_notice', 'text DEFAULT NULL COMMENT ''兑换与履约说明''');
CALL add_column_if_missing('mall_product', 'sale_points', 'int NOT NULL DEFAULT 0 COMMENT ''兑换所需绿豆''');
CALL add_column_if_missing('mall_product', 'market_amount', 'int NOT NULL DEFAULT 0 COMMENT ''市场价，单位分''');
CALL add_column_if_missing('mall_product', 'stock_total', 'int NOT NULL DEFAULT 0 COMMENT ''总库存''');
CALL add_column_if_missing('mall_product', 'stock_available', 'int NOT NULL DEFAULT 0 COMMENT ''可用库存''');
CALL add_column_if_missing('mall_product', 'sold_qty', 'int NOT NULL DEFAULT 0 COMMENT ''累计兑换数量''');
CALL add_column_if_missing('mall_product', 'fulfillment_type', 'varchar(16) NOT NULL DEFAULT ''SHIP'' COMMENT ''SHIP/PICKUP/VIRTUAL''');
CALL add_column_if_missing('mall_product', 'merchant_id', 'bigint DEFAULT NULL COMMENT ''履约商家ID''');
CALL add_column_if_missing('mall_product', 'merchant_name', 'varchar(150) DEFAULT NULL COMMENT ''履约商家名称''');
CALL add_column_if_missing('mall_product', 'pickup_address', 'varchar(500) DEFAULT NULL COMMENT ''到店领取地址''');
CALL add_column_if_missing('mall_product', 'per_user_limit', 'int NOT NULL DEFAULT 0 COMMENT ''每人累计限兑，0不限制''');
CALL add_column_if_missing('mall_product', 'max_qty_per_order', 'int NOT NULL DEFAULT 1 COMMENT ''单次最大兑换数量''');
CALL add_column_if_missing('mall_product', 'require_address', 'tinyint NOT NULL DEFAULT 1 COMMENT ''是否需要地址''');
CALL add_column_if_missing('mall_product', 'published_at', 'datetime DEFAULT NULL COMMENT ''上架时间''');
CALL add_column_if_missing('mall_product', 'status', 'tinyint NOT NULL DEFAULT 0 COMMENT ''1上架0下架''');
CALL add_column_if_missing('mall_product', 'sort_no', 'int NOT NULL DEFAULT 0 COMMENT ''排序号''');
CALL add_column_if_missing('mall_product', 'ext_json', 'text DEFAULT NULL COMMENT ''扩展配置JSON''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

UPDATE mall_product
   SET product_type = 'OTHER'
 WHERE product_type IS NULL OR product_type = '';

UPDATE mall_product
   SET fulfillment_type = 'SHIP'
 WHERE fulfillment_type IS NULL OR fulfillment_type NOT IN ('SHIP', 'PICKUP', 'VIRTUAL');

UPDATE mall_product
   SET sold_qty = 0
 WHERE sold_qty IS NULL OR sold_qty < 0;

UPDATE mall_product
   SET stock_total = GREATEST(IFNULL(stock_total, 0), 0),
       stock_available = GREATEST(IFNULL(stock_available, 0), 0),
       sale_points = GREATEST(IFNULL(sale_points, 0), 0),
       market_amount = GREATEST(IFNULL(market_amount, 0), 0),
       max_qty_per_order = CASE WHEN max_qty_per_order IS NULL OR max_qty_per_order <= 0 THEN 1 ELSE max_qty_per_order END,
       per_user_limit = GREATEST(IFNULL(per_user_limit, 0), 0);

-- 执行后验证：
-- SHOW COLUMNS FROM mall_product;
-- SELECT id, product_name, stock_available, sold_qty, fulfillment_type, status
--   FROM mall_product WHERE is_deleted = 0 LIMIT 10;
