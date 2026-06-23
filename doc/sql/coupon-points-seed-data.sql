-- 测试数据脚本：绿豆/优惠券/商城联调用
-- 建议先执行结构脚本: coupon-points-growth-migration.sql
-- 注意：以下 user_id 请替换为你本地已存在的真实用户ID

SET NAMES utf8mb4;

-- 可按需清理（联调环境使用）
-- DELETE FROM coupon_verify_log;
-- DELETE FROM mall_exchange_order;
-- DELETE FROM mall_product;
-- DELETE FROM user_coupon;
-- DELETE FROM coupon_receive_log;
-- DELETE FROM coupon_template;
-- DELETE FROM points_signin_stat;
-- DELETE FROM points_ledger;
-- DELETE FROM points_task_log;
-- DELETE FROM points_daily_counter;
-- DELETE FROM points_rule;
-- DELETE FROM points_account;
-- DELETE FROM growth_level_config;

-- 假定联调用户
-- 普通用户: 10001
-- 商家用户: 10002

INSERT INTO growth_level_config(id, level_no, level_name, min_earned_points, icon_url, privilege_desc, status, create_time, update_time, create_user, update_user, is_deleted, tenant_id)
VALUES
(1,1,'成长1级',100,NULL,'可领取基础成长券',1,NOW(),NOW(),1,1,0,'000000'),
(2,2,'成长2级',1000,NULL,'可领取2级专属券',1,NOW(),NOW(),1,1,0,'000000'),
(3,3,'成长3级',3000,NULL,'可领取3级专属券',1,NOW(),NOW(),1,1,0,'000000'),
(4,4,'成长4级',8000,NULL,'可领取高阶券',1,NOW(),NOW(),1,1,0,'000000'),
(5,5,'成长5级',20000,NULL,'可领取顶级券',1,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE level_name=VALUES(level_name), min_earned_points=VALUES(min_earned_points), privilege_desc=VALUES(privilege_desc), status=VALUES(status), update_time=NOW();

INSERT INTO points_rule(id, rule_code, rule_name, scene_type, grant_points, daily_limit_count, daily_limit_points, lifecycle_limit_count, require_first_flag, status, ext_json, create_time, update_time, create_user, update_user, is_deleted, tenant_id)
VALUES
(101,'DAILY_SIGNIN','每日签到','SIGNIN',5,1,5,NULL,0,1,NULL,NOW(),NOW(),1,1,0,'000000'),
(102,'CONTENT_BROWSE','浏览内容','BROWSE',1,10,10,NULL,0,1,NULL,NOW(),NOW(),1,1,0,'000000'),
(103,'TRAINING_SIGNUP_SUCCESS','培训报名成功','TRAINING',20,NULL,NULL,NULL,0,1,NULL,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE rule_name=VALUES(rule_name), grant_points=VALUES(grant_points), status=VALUES(status), update_time=NOW();

INSERT INTO points_account(id, user_id, available_points, frozen_points, total_earned_points, total_spent_points, growth_level, version, create_time, update_time, create_user, update_user, is_deleted, tenant_id)
VALUES
(10001,10001,5200,0,8600,3400,4,0,NOW(),NOW(),1,1,0,'000000'),
(10002,10002,1800,0,2200,400,3,0,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE available_points=VALUES(available_points), total_earned_points=VALUES(total_earned_points), total_spent_points=VALUES(total_spent_points), growth_level=VALUES(growth_level), update_time=NOW();

INSERT INTO points_signin_stat(id, user_id, last_signin_date, continue_days, month_signin_days, create_time, update_time, create_user, update_user, is_deleted, tenant_id)
VALUES
(20001,10001,CURDATE(),6,12,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE last_signin_date=VALUES(last_signin_date), continue_days=VALUES(continue_days), month_signin_days=VALUES(month_signin_days), update_time=NOW();

INSERT INTO points_ledger(id, user_id, change_type, change_points, before_points, after_points, rule_code, biz_type, biz_id, remark, expires_at, request_id, create_time, update_time, create_user, update_user, is_deleted, tenant_id)
VALUES
(30001,10001,'INCOME',2000,3200,5200,'TRAINING_SIGNUP_SUCCESS','TRAINING_SIGNUP','TRN_20260514001','培训报名奖励',DATE_ADD(NOW(), INTERVAL 12 MONTH),'REQ_LEDGER_001',NOW(),NOW(),1,1,0,'000000'),
(30002,10001,'SPEND',-800,6000,5200,NULL,'MALL_EXCHANGE','MO20260514001','商城兑换扣减',NULL,'REQ_LEDGER_002',NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE remark=VALUES(remark), update_time=NOW();

INSERT INTO coupon_template(
  id,coupon_code,coupon_name,coupon_type,benefit_mode,threshold_amount,discount_amount,max_discount_amount,
  duration_minutes,total_times,deduct_target_type,deduct_target_id,deduct_unit_amount,
  scope_type,scope_ref_id,total_stock,remain_stock,per_user_limit,min_growth_level,
  valid_type,valid_start_at,valid_end_at,valid_days,acquire_type,cost_points,status,ext_json,
  create_time,update_time,create_user,update_user,is_deleted,tenant_id
)
VALUES
(40001,'CP_TPL_CASH_50','满300减50券','CASH','AMOUNT',30000,5000,NULL,NULL,NULL,NULL,NULL,NULL,'ALL',NULL,1000,998,1,1,'FIXED',NOW(),DATE_ADD(NOW(),INTERVAL 60 DAY),NULL,'FREE',0,1,NULL,NOW(),NOW(),1,1,0,'000000'),
(40002,'CP_TPL_GYM_120','2小时健身券','DURATION','DURATION',0,0,NULL,120,NULL,'SERVICE','GYM',NULL,'VENUE','GYM_A',500,499,2,2,'RELATIVE',NULL,NULL,30,'FREE',0,1,'{"receive_auth_required":false}',NOW(),NOW(),1,1,0,'000000'),
(40003,'CP_TPL_SKU_10','指定商品抵扣10元','SKU_DEDUCT','SKU_DEDUCT',0,1000,NULL,NULL,NULL,'SKU','SKU1001',1000,'GOODS','SKU1001',300,300,1,3,'FIXED',NOW(),DATE_ADD(NOW(),INTERVAL 45 DAY),NULL,'POINTS_EXCHANGE',300,1,NULL,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE coupon_name=VALUES(coupon_name), remain_stock=VALUES(remain_stock), min_growth_level=VALUES(min_growth_level), status=VALUES(status), update_time=NOW();

INSERT INTO coupon_receive_log(id, request_id, user_id, coupon_template_id, receive_channel, status, fail_reason, create_time, update_time, create_user, update_user, is_deleted, tenant_id)
VALUES
(50001,'REQ_RECV_001',10001,40001,'APP',1,NULL,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE status=VALUES(status), update_time=NOW();

INSERT INTO user_coupon(
  id,user_id,coupon_template_id,coupon_no,status,remain_duration_minutes,remain_times,
  valid_start_at,valid_end_at,locked_order_no,used_order_no,used_at,verify_merchant_user_id,verify_at,
  create_time,update_time,create_user,update_user,is_deleted,tenant_id
)
VALUES
(60001,10001,40001,'UCP10001A','UNUSED',NULL,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL,NULL,NULL,NULL,NULL,NOW(),NOW(),1,1,0,'000000'),
(60002,10001,40002,'UCP10001B','UNUSED',120,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL,NULL,NULL,NULL,NULL,NOW(),NOW(),1,1,0,'000000'),
(60003,10001,40003,'UCP10001C','LOCKED',NULL,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 20 DAY),'ORD_LOCK_001',NULL,NULL,NULL,NULL,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE status=VALUES(status), remain_duration_minutes=VALUES(remain_duration_minutes), locked_order_no=VALUES(locked_order_no), update_time=NOW();

INSERT INTO mall_product(
  id,product_code,product_name,product_desc,product_type,cover_url,sale_points,market_amount,
  stock_total,stock_available,status,sort_no,ext_json,
  create_time,update_time,create_user,update_user,is_deleted,tenant_id
)
VALUES
(70001,'MP_BALL_001','训练篮球','7号标准训练篮球','PHYSICAL',NULL,800,12900,200,198,1,10,NULL,NOW(),NOW(),1,1,0,'000000'),
(70002,'MP_CARD_002','月卡体验券','兑换后发放体验权益','VIRTUAL',NULL,1200,19900,1000,1000,1,20,NULL,NOW(),NOW(),1,1,0,'000000'),
(70003,'MP_TOWEL_003','速干运动毛巾','吸汗速干','PHYSICAL',NULL,300,3900,500,500,1,30,NULL,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE product_name=VALUES(product_name), sale_points=VALUES(sale_points), stock_available=VALUES(stock_available), status=VALUES(status), update_time=NOW();

INSERT INTO mall_exchange_order(
  id,order_no,request_id,user_id,product_id,qty,spend_points,status,fail_reason,delivery_status,
  create_time,update_time,create_user,update_user,is_deleted,tenant_id
)
VALUES
(80001,'MO20260514001','REQ_EX_001',10001,70001,1,800,'SUCCESS',NULL,'PENDING',NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE status=VALUES(status), delivery_status=VALUES(delivery_status), update_time=NOW();

INSERT INTO coupon_verify_log(
  id,user_coupon_id,user_id,merchant_user_id,template_id,coupon_no,verify_channel,verify_result,fail_reason,status,verify_status,order_no,ext_json,
  create_time,update_time,create_user,update_user,is_deleted,tenant_id
)
VALUES
(90001,60002,10001,10002,40002,'UCP10001B','APP',1,NULL,1,'FINISHED','GYM_ORDER_001',NULL,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE verify_result=VALUES(verify_result), status=VALUES(status), verify_status=VALUES(verify_status), update_time=NOW();

INSERT INTO rule_version_notice(
  id,module_type,version_no,notice_title,notice_content,publish_at,effective_at,status,
  create_time,update_time,create_user,update_user,is_deleted,tenant_id
)
VALUES
(100001,'POINTS','v1.0.0','绿豆规则上线公告','新增签到、内容互动、报名奖励等规则',NOW(),DATE_ADD(NOW(), INTERVAL 7 DAY),1,NOW(),NOW(),1,1,0,'000000'),
(100002,'COUPON','v1.0.0','优惠券规则上线公告','新增现金券、时长券、指定商品抵扣券',NOW(),DATE_ADD(NOW(), INTERVAL 7 DAY),1,NOW(),NOW(),1,1,0,'000000')
ON DUPLICATE KEY UPDATE notice_title=VALUES(notice_title), notice_content=VALUES(notice_content), update_time=NOW();

SELECT 'seed done' AS result;
