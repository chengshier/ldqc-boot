-- market-ready-phase1 已确认历史脏数据清理
-- 适用：MySQL 5.7
-- 前置：先执行本批结构迁移。执行前备份 user_coupon、coupon_verify_log、mall_exchange_order、ldqc_training。
-- 原则：只处理已确认的无效状态和明显误入课程标题的数据，不猜测或重写正常业务内容。

SET NAMES utf8mb4;

-- 1. 用户券：BladeX status 恢复数值启停语义，券业务状态只存 coupon_status。
UPDATE user_coupon
   SET coupon_status = UPPER(CAST(status AS CHAR))
 WHERE UPPER(CAST(status AS CHAR)) IN ('UNUSED','LOCKED','PARTIAL_USED','USED','EXPIRED','INVALID')
   AND (coupon_status IS NULL OR coupon_status = '' OR coupon_status = 'UNUSED');

UPDATE user_coupon
   SET coupon_status = 'UNUSED'
 WHERE coupon_status IS NULL
    OR coupon_status = ''
    OR UPPER(coupon_status) NOT IN ('UNUSED','LOCKED','PARTIAL_USED','USED','EXPIRED','INVALID');

UPDATE user_coupon SET coupon_status = UPPER(coupon_status);
UPDATE user_coupon SET status = 1 WHERE status IS NULL OR CAST(status AS CHAR) <> '0';

-- 2. 核销日志：历史成功日志默认视为已完成。
UPDATE coupon_verify_log
   SET verify_status = 'FINISHED'
 WHERE verify_status IS NULL
    OR TRIM(verify_status) = ''
    OR UPPER(verify_status) NOT IN ('PROCESSING','FINISHED');
UPDATE coupon_verify_log SET verify_status = UPPER(verify_status);

-- 3. 商城订单：独立业务状态，不再复用通用 status。
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
UPDATE mall_exchange_order SET order_status = UPPER(order_status), status = COALESCE(status, 1);

-- 4. 课程：文件名、压缩包名或项目排期文档误入课程标题时，先停用并退回草稿等待运营核对。
-- 不自动删除记录，也不擅自生成新课程名。
UPDATE ldqc_training
   SET status = 0,
       publish_status = 'DRAFT',
       audit_reason = CONCAT(
           '历史异常课程标题，疑似上传文件名误写入标题；请运营核对原始数据。原值：',
           LEFT(COALESCE(title, ''), 300)
       )
 WHERE is_deleted = 0
   AND (
       LOWER(TRIM(title)) REGEXP '\\.(doc|docx|xls|xlsx|ppt|pptx|pdf|zip|rar|7z|mp4|mov|avi)$'
       OR title LIKE '%小程序上线倒计时排期表%'
   );

UPDATE ldqc_training
   SET content_mode = CASE
       WHEN UPPER(content_mode) IN ('OFFLINE','ONLINE','MIXED') THEN UPPER(content_mode)
       ELSE 'OFFLINE'
   END,
       publish_status = CASE
       WHEN UPPER(publish_status) IN ('DRAFT','PENDING','PUBLISHED','REJECTED','OFFLINE') THEN UPPER(publish_status)
       ELSE CASE WHEN status = 1 THEN 'PUBLISHED' ELSE 'OFFLINE' END
   END
 WHERE is_deleted = 0;

-- 5. 执行后核对。
SELECT status, coupon_status, COUNT(*) AS row_count
  FROM user_coupon
 GROUP BY status, coupon_status
 ORDER BY status, coupon_status;

SELECT verify_status, COUNT(*) AS row_count
  FROM coupon_verify_log
 GROUP BY verify_status
 ORDER BY verify_status;

SELECT status, order_status, fulfillment_status, COUNT(*) AS row_count
  FROM mall_exchange_order
 GROUP BY status, order_status, fulfillment_status
 ORDER BY status, order_status, fulfillment_status;

SELECT id, title, status, publish_status, audit_reason
  FROM ldqc_training
 WHERE audit_reason LIKE '历史异常课程标题%'
 ORDER BY update_time DESC, id DESC;
