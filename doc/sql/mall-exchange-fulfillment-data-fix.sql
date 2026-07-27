-- 商城履约历史数据规范化
-- 在 mall-exchange-fulfillment-migration.sql 之后执行。

SET NAMES utf8mb4;

UPDATE mall_product
   SET product_type = 'OTHER'
 WHERE product_type IS NULL OR product_type = '';

UPDATE mall_product
   SET fulfillment_type = 'SHIP'
 WHERE fulfillment_type IS NULL OR fulfillment_type NOT IN ('SHIP', 'PICKUP', 'VIRTUAL');

UPDATE mall_product
   SET max_qty_per_order = 1
 WHERE max_qty_per_order IS NULL OR max_qty_per_order <= 0;

UPDATE mall_product
   SET per_user_limit = 0
 WHERE per_user_limit IS NULL OR per_user_limit < 0;

UPDATE mall_product
   SET require_address = CASE WHEN fulfillment_type = 'SHIP' THEN 1 ELSE 0 END
 WHERE require_address IS NULL;

UPDATE mall_exchange_order
   SET fulfillment_status = 'PENDING'
 WHERE fulfillment_status IS NULL OR fulfillment_status = '';
