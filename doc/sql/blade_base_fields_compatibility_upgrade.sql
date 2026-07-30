-- market-ready-phase1 新增/改造表 BladeX 基础字段兼容补丁
-- 适用：MySQL 5.7
-- 说明：仅补充缺失的 TenantEntity/BaseEntity 非主键字段，不擅自重建主键或覆盖已有字段类型。
-- 执行前请备份下列业务表，并在测试库先执行。

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

DROP PROCEDURE IF EXISTS ensure_blade_fields;
DELIMITER $$
CREATE PROCEDURE ensure_blade_fields(IN p_table VARCHAR(64))
BEGIN
    CALL add_column_if_missing(p_table, 'tenant_id', 'varchar(12) NOT NULL DEFAULT ''000000'' COMMENT ''租户ID''');
    CALL add_column_if_missing(p_table, 'create_user', 'bigint DEFAULT NULL COMMENT ''创建人''');
    CALL add_column_if_missing(p_table, 'create_dept', 'bigint DEFAULT NULL COMMENT ''创建部门''');
    CALL add_column_if_missing(p_table, 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''');
    CALL add_column_if_missing(p_table, 'update_user', 'bigint DEFAULT NULL COMMENT ''修改人''');
    CALL add_column_if_missing(p_table, 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''修改时间''');
    CALL add_column_if_missing(p_table, 'status', 'int NOT NULL DEFAULT 1 COMMENT ''数据状态 1正常0停用''');
    CALL add_column_if_missing(p_table, 'is_deleted', 'int NOT NULL DEFAULT 0 COMMENT ''逻辑删除 0正常1删除''');
END$$
DELIMITER ;

-- 本分支新建表。
CALL ensure_blade_fields('content_audit_task');
CALL ensure_blade_fields('ldqc_recommend_feedback');
CALL ensure_blade_fields('ldqc_training_chapter');
CALL ensure_blade_fields('ldqc_training_lesson');
CALL ensure_blade_fields('ldqc_training_access');
CALL ensure_blade_fields('ldqc_training_progress');
CALL ensure_blade_fields('ldqc_venue_apply');
CALL ensure_blade_fields('coupon_verifier_scope');

-- 本分支重点改造且 Entity 继承 TenantEntity 的表。
CALL ensure_blade_fields('coupon_verify_log');
CALL ensure_blade_fields('coupon_receive_log');
CALL ensure_blade_fields('coupon_template');
CALL ensure_blade_fields('user_coupon');
CALL ensure_blade_fields('mall_product');
CALL ensure_blade_fields('mall_exchange_order');
CALL ensure_blade_fields('ldqc_competition');
CALL ensure_blade_fields('ldqc_competition_signup');
CALL ensure_blade_fields('ldqc_training');
CALL ensure_blade_fields('ldqc_venue');
CALL ensure_blade_fields('ldqc_talent_post');
CALL ensure_blade_fields('t_img_detail');
CALL ensure_blade_fields('t_follow');

DROP PROCEDURE IF EXISTS ensure_blade_fields;
DROP PROCEDURE IF EXISTS add_column_if_missing;

-- 补齐新增字段后的空值。只处理基础字段，不改业务状态字段。
DROP PROCEDURE IF EXISTS normalize_blade_fields;
DELIMITER $$
CREATE PROCEDURE normalize_blade_fields(IN p_table VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table
    ) THEN
        SET @ddl = CONCAT(
            'UPDATE `', p_table, '` SET ',
            'tenant_id = COALESCE(NULLIF(tenant_id, ''''), ''000000''), ',
            'status = COALESCE(status, 1), ',
            'is_deleted = COALESCE(is_deleted, 0), ',
            'create_time = COALESCE(create_time, NOW()), ',
            'update_time = COALESCE(update_time, create_time, NOW())'
        );
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL normalize_blade_fields('content_audit_task');
CALL normalize_blade_fields('ldqc_recommend_feedback');
CALL normalize_blade_fields('ldqc_training_chapter');
CALL normalize_blade_fields('ldqc_training_lesson');
CALL normalize_blade_fields('ldqc_training_access');
CALL normalize_blade_fields('ldqc_training_progress');
CALL normalize_blade_fields('ldqc_venue_apply');
CALL normalize_blade_fields('coupon_verifier_scope');
CALL normalize_blade_fields('coupon_verify_log');
CALL normalize_blade_fields('coupon_receive_log');
CALL normalize_blade_fields('coupon_template');
CALL normalize_blade_fields('user_coupon');
CALL normalize_blade_fields('mall_product');
CALL normalize_blade_fields('mall_exchange_order');
CALL normalize_blade_fields('ldqc_competition');
CALL normalize_blade_fields('ldqc_competition_signup');
CALL normalize_blade_fields('ldqc_training');
CALL normalize_blade_fields('ldqc_venue');
CALL normalize_blade_fields('ldqc_talent_post');
CALL normalize_blade_fields('t_img_detail');
CALL normalize_blade_fields('t_follow');

DROP PROCEDURE IF EXISTS normalize_blade_fields;

-- 输出仍需人工处理的基础字段问题。
SELECT t.TABLE_NAME,
       GROUP_CONCAT(required.column_name ORDER BY required.sort_no SEPARATOR ',') AS missing_columns
  FROM information_schema.TABLES t
  JOIN (
      SELECT 1 sort_no, 'id' column_name UNION ALL
      SELECT 2, 'tenant_id' UNION ALL
      SELECT 3, 'create_user' UNION ALL
      SELECT 4, 'create_dept' UNION ALL
      SELECT 5, 'create_time' UNION ALL
      SELECT 6, 'update_user' UNION ALL
      SELECT 7, 'update_time' UNION ALL
      SELECT 8, 'status' UNION ALL
      SELECT 9, 'is_deleted'
  ) required
 WHERE t.TABLE_SCHEMA = DATABASE()
   AND t.TABLE_NAME IN (
       'content_audit_task','ldqc_recommend_feedback','ldqc_training_chapter','ldqc_training_lesson',
       'ldqc_training_access','ldqc_training_progress','ldqc_venue_apply','coupon_verifier_scope',
       'coupon_verify_log','coupon_receive_log','coupon_template','user_coupon','mall_product',
       'mall_exchange_order','ldqc_competition','ldqc_competition_signup','ldqc_training',
       'ldqc_venue','ldqc_talent_post','t_img_detail','t_follow'
   )
   AND NOT EXISTS (
       SELECT 1 FROM information_schema.COLUMNS c
        WHERE c.TABLE_SCHEMA = t.TABLE_SCHEMA
          AND c.TABLE_NAME = t.TABLE_NAME
          AND c.COLUMN_NAME = required.column_name
   )
 GROUP BY t.TABLE_NAME
 ORDER BY t.TABLE_NAME;
