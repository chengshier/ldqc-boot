-- market-ready-phase1 数据库结构验收矩阵
-- 适用：MySQL 5.7
-- 只读脚本：不修改业务表，用于执行迁移后检查表、BladeX 基础字段、业务状态字段和关键索引。

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_tables;
CREATE TEMPORARY TABLE tmp_market_ready_tables (
  table_name varchar(64) PRIMARY KEY,
  entity_name varchar(128) NOT NULL,
  base_class varchar(64) NOT NULL
) ENGINE=Memory;

INSERT INTO tmp_market_ready_tables(table_name, entity_name, base_class) VALUES
('content_audit_task', 'ContentAuditTaskEntity', 'TenantEntity'),
('ldqc_recommend_feedback', 'RecommendFeedbackEntity', 'TenantEntity'),
('ldqc_training_chapter', 'TrainingChapterEntity', 'TenantEntity'),
('ldqc_training_lesson', 'TrainingLessonEntity', 'TenantEntity'),
('ldqc_training_access', 'TrainingAccessEntity', 'TenantEntity'),
('ldqc_training_progress', 'TrainingProgressEntity', 'TenantEntity'),
('ldqc_venue_apply', 'VenueApplyEntity', 'TenantEntity'),
('coupon_verifier_scope', 'CouponVerifierScopeEntity', 'TenantEntity'),
('coupon_verify_log', 'CouponVerifyLogEntity', 'TenantEntity'),
('coupon_receive_log', 'CouponReceiveLogEntity', 'TenantEntity'),
('coupon_template', 'CouponTemplateEntity', 'TenantEntity'),
('user_coupon', 'UserCouponEntity', 'TenantEntity'),
('mall_product', 'MallProductEntity', 'TenantEntity'),
('mall_exchange_order', 'MallExchangeOrderEntity', 'TenantEntity'),
('ldqc_competition', 'CompetitionEntity', 'TenantEntity'),
('ldqc_competition_signup', 'CompetitionSignupEntity', 'TenantEntity'),
('ldqc_training', 'TrainingEntity', 'TenantEntity'),
('ldqc_venue', 'VenueEntity', 'TenantEntity'),
('ldqc_talent_post', 'TalentPostEntity', 'TenantEntity'),
('t_img_detail', 'ImgDetailEntity', 'TenantEntity'),
('t_follow', 'FollowEntity', 'TenantEntity');

-- 1. 表存在性
SELECT t.table_name,
       t.entity_name,
       t.base_class,
       CASE WHEN db.TABLE_NAME IS NULL THEN '缺表' ELSE '存在' END AS table_conclusion
  FROM tmp_market_ready_tables t
  LEFT JOIN information_schema.TABLES db
    ON db.TABLE_SCHEMA = DATABASE()
   AND db.TABLE_NAME = t.table_name
 ORDER BY t.table_name;

DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_base_columns;
CREATE TEMPORARY TABLE tmp_market_ready_base_columns (
  sort_no int NOT NULL,
  column_name varchar(64) NOT NULL,
  expected_family varchar(32) NOT NULL,
  PRIMARY KEY(column_name)
) ENGINE=Memory;

INSERT INTO tmp_market_ready_base_columns VALUES
(1, 'id', 'BIGINT'),
(2, 'tenant_id', 'VARCHAR'),
(3, 'create_user', 'INTEGER'),
(4, 'create_dept', 'INTEGER'),
(5, 'create_time', 'DATETIME'),
(6, 'update_user', 'INTEGER'),
(7, 'update_time', 'DATETIME'),
(8, 'status', 'INTEGER'),
(9, 'is_deleted', 'INTEGER');

-- 2. BladeX 基础字段矩阵
SELECT t.table_name,
       t.entity_name,
       t.base_class,
       b.column_name,
       b.expected_family,
       c.COLUMN_TYPE AS actual_column_type,
       c.IS_NULLABLE,
       c.COLUMN_DEFAULT,
       CASE
         WHEN db.TABLE_NAME IS NULL THEN '缺表'
         WHEN c.COLUMN_NAME IS NULL THEN '缺字段'
         WHEN b.expected_family = 'BIGINT' AND c.DATA_TYPE <> 'bigint' THEN '类型不一致'
         WHEN b.expected_family = 'VARCHAR' AND c.DATA_TYPE NOT IN ('varchar','char') THEN '类型不一致'
         WHEN b.expected_family = 'DATETIME' AND c.DATA_TYPE NOT IN ('datetime','timestamp') THEN '类型不一致'
         WHEN b.expected_family = 'INTEGER' AND c.DATA_TYPE NOT IN ('tinyint','smallint','mediumint','int','bigint') THEN '类型不一致'
         WHEN b.column_name = 'status' AND c.DATA_TYPE NOT IN ('tinyint','smallint','mediumint','int','bigint') THEN '业务状态误用'
         ELSE '完整'
       END AS conclusion
  FROM tmp_market_ready_tables t
 CROSS JOIN tmp_market_ready_base_columns b
  LEFT JOIN information_schema.TABLES db
    ON db.TABLE_SCHEMA = DATABASE()
   AND db.TABLE_NAME = t.table_name
  LEFT JOIN information_schema.COLUMNS c
    ON c.TABLE_SCHEMA = DATABASE()
   AND c.TABLE_NAME = t.table_name
   AND c.COLUMN_NAME = b.column_name
 ORDER BY t.table_name, b.sort_no;

DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_business_columns;
CREATE TEMPORARY TABLE tmp_market_ready_business_columns (
  table_name varchar(64) NOT NULL,
  column_name varchar(64) NOT NULL,
  expected_family varchar(32) NOT NULL,
  purpose varchar(200) NOT NULL,
  PRIMARY KEY(table_name, column_name)
) ENGINE=Memory;

INSERT INTO tmp_market_ready_business_columns VALUES
('ldqc_recommend_feedback', 'request_id', 'VARCHAR', '推荐反馈幂等请求号'),
('ldqc_recommend_feedback', 'content_type', 'VARCHAR', 'CONTENT/NEWS'),
('ldqc_recommend_feedback', 'event_type', 'VARCHAR', '推荐行为类型'),
('ldqc_recommend_feedback', 'occurred_at', 'DATETIME', '反馈发生时间'),
('coupon_verify_log', 'verify_status', 'VARCHAR', 'PROCESSING/FINISHED'),
('user_coupon', 'coupon_status', 'VARCHAR', 'UNUSED/LOCKED/PARTIAL_USED/USED/EXPIRED/INVALID'),
('mall_exchange_order', 'order_status', 'VARCHAR', 'CREATED/SUCCESS/FAILED/CANCELLED/COMPLETED'),
('ldqc_training', 'publish_status', 'VARCHAR', '课程发布状态'),
('ldqc_training', 'content_mode', 'VARCHAR', 'OFFLINE/ONLINE/MIXED'),
('ldqc_training_access', 'access_status', 'VARCHAR', '播放授权业务状态'),
('ldqc_training_progress', 'completed', 'INTEGER', '学习完成标志'),
('ldqc_venue_apply', 'apply_status', 'VARCHAR', '场馆入驻申请状态'),
('ldqc_competition_signup', 'order_status', 'VARCHAR', '赛事报名订单状态'),
('content_audit_task', 'audit_status', 'INTEGER', '内容审核业务状态');

-- 3. 关键业务字段矩阵
SELECT e.table_name,
       e.column_name,
       e.expected_family,
       e.purpose,
       c.COLUMN_TYPE AS actual_column_type,
       c.IS_NULLABLE,
       c.COLUMN_DEFAULT,
       CASE
         WHEN t.TABLE_NAME IS NULL THEN '缺表'
         WHEN c.COLUMN_NAME IS NULL THEN '缺字段'
         WHEN e.expected_family = 'VARCHAR' AND c.DATA_TYPE NOT IN ('varchar','char','text') THEN '类型不一致'
         WHEN e.expected_family = 'DATETIME' AND c.DATA_TYPE NOT IN ('datetime','timestamp') THEN '类型不一致'
         WHEN e.expected_family = 'INTEGER' AND c.DATA_TYPE NOT IN ('tinyint','smallint','mediumint','int','bigint') THEN '类型不一致'
         ELSE '完整'
       END AS conclusion
  FROM tmp_market_ready_business_columns e
  LEFT JOIN information_schema.TABLES t
    ON t.TABLE_SCHEMA = DATABASE() AND t.TABLE_NAME = e.table_name
  LEFT JOIN information_schema.COLUMNS c
    ON c.TABLE_SCHEMA = DATABASE()
   AND c.TABLE_NAME = e.table_name
   AND c.COLUMN_NAME = e.column_name
 ORDER BY e.table_name, e.column_name;

-- 4. 通用 status 是否仍被业务字符串污染
SELECT 'user_coupon' AS table_name,
       c.COLUMN_TYPE,
       CASE WHEN c.DATA_TYPE IN ('tinyint','smallint','mediumint','int','bigint') THEN '通过' ELSE '失败：status 必须为数值' END AS conclusion
  FROM information_schema.COLUMNS c
 WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = 'user_coupon' AND c.COLUMN_NAME = 'status'
UNION ALL
SELECT 'mall_exchange_order',
       c.COLUMN_TYPE,
       CASE WHEN c.DATA_TYPE IN ('tinyint','smallint','mediumint','int','bigint') THEN '通过' ELSE '失败：status 必须为数值' END
  FROM information_schema.COLUMNS c
 WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = 'mall_exchange_order' AND c.COLUMN_NAME = 'status';

-- 5. 关键索引
DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_indexes;
CREATE TEMPORARY TABLE tmp_market_ready_indexes (
  table_name varchar(64) NOT NULL,
  index_name varchar(128) NOT NULL,
  purpose varchar(200) NOT NULL,
  PRIMARY KEY(table_name, index_name)
) ENGINE=Memory;

INSERT INTO tmp_market_ready_indexes VALUES
('ldqc_recommend_feedback', 'uk_recommend_feedback_user_request', '推荐反馈幂等'),
('coupon_verify_log', 'idx_coupon_verify_log_coupon_status', '券核销记录查询'),
('mall_exchange_order', 'uk_mall_order_user_request', '商城兑换幂等'),
('mall_exchange_order', 'idx_mall_order_business_status', '商城业务状态与履约查询'),
('ldqc_training_access', 'uk_training_access_user_course', '课程授权唯一性'),
('ldqc_training_progress', 'uk_training_progress_user_lesson', '学习进度唯一性'),
('ldqc_venue_apply', 'uk_venue_apply_request_no', '场馆申请幂等'),
('ldqc_competition_signup', 'uk_comp_signup_user_request', '赛事报名幂等'),
('content_audit_task', 'idx_content_audit_retry', '内容审核重试调度');

SELECT e.table_name,
       e.index_name,
       e.purpose,
       CASE WHEN s.INDEX_NAME IS NULL THEN '缺索引' ELSE '存在' END AS conclusion
  FROM tmp_market_ready_indexes e
  LEFT JOIN (
      SELECT DISTINCT TABLE_NAME, INDEX_NAME
        FROM information_schema.STATISTICS
       WHERE TABLE_SCHEMA = DATABASE()
  ) s ON s.TABLE_NAME = e.table_name AND s.INDEX_NAME = e.index_name
 ORDER BY e.table_name, e.index_name;

DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_indexes;
DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_business_columns;
DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_base_columns;
DROP TEMPORARY TABLE IF EXISTS tmp_market_ready_tables;
