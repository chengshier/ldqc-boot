-- 场馆入驻申请与运营账号绑定升级
-- 适用：MySQL 5.7
-- 执行前请备份 ldqc_venue。

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

CALL add_column_if_missing('ldqc_venue', 'owner_user_id', 'bigint DEFAULT NULL COMMENT ''场馆运营用户ID''');
CALL add_column_if_missing('ldqc_venue', 'source_apply_id', 'bigint DEFAULT NULL COMMENT ''来源入驻申请ID''');
CALL add_column_if_missing('ldqc_venue', 'merchant_name', 'varchar(150) DEFAULT NULL COMMENT ''场馆经营主体名称''');
CALL add_column_if_missing('ldqc_venue', 'service_notice', 'varchar(1000) DEFAULT NULL COMMENT ''预约、入场和服务说明''');
DROP PROCEDURE IF EXISTS add_column_if_missing;

CREATE TABLE IF NOT EXISTS ldqc_venue_apply (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000',
  request_no varchar(40) NOT NULL COMMENT '申请单号',
  active_unique_key varchar(80) DEFAULT NULL COMMENT '待审核申请唯一键',
  applicant_user_id bigint NOT NULL COMMENT '申请用户ID',
  applicant_name varchar(50) NOT NULL COMMENT '联系人姓名',
  applicant_phone varchar(30) NOT NULL COMMENT '联系人手机号',
  merchant_name varchar(150) NOT NULL COMMENT '经营主体名称',
  license_no varchar(100) NOT NULL COMMENT '营业执照编号',
  license_image varchar(1000) NOT NULL COMMENT '营业执照图片',
  venue_name varchar(150) NOT NULL COMMENT '场馆名称',
  venue_type_id bigint DEFAULT NULL COMMENT '场馆类型ID',
  cover_image varchar(1000) DEFAULT NULL COMMENT '场馆封面',
  images varchar(4000) DEFAULT NULL COMMENT '场馆图集',
  address varchar(500) NOT NULL COMMENT '场馆地址',
  longitude decimal(11,7) DEFAULT NULL COMMENT '经度',
  latitude decimal(10,7) DEFAULT NULL COMMENT '纬度',
  business_hours varchar(200) DEFAULT NULL COMMENT '营业时间',
  venue_phone varchar(50) DEFAULT NULL COMMENT '场馆对外电话',
  tags varchar(300) DEFAULT NULL COMMENT '场馆标签',
  description text NULL COMMENT '场馆介绍',
  service_notice varchar(1000) DEFAULT NULL COMMENT '服务说明',
  apply_status varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED',
  audit_user_id bigint DEFAULT NULL COMMENT '审核人ID',
  audit_time datetime DEFAULT NULL COMMENT '审核时间',
  audit_reason varchar(500) DEFAULT NULL COMMENT '审核说明',
  venue_id bigint DEFAULT NULL COMMENT '审核通过后创建的场馆ID',
  submitted_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  status int NOT NULL DEFAULT 1,
  create_user bigint DEFAULT NULL,
  create_dept bigint DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_user bigint DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted int NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_venue_apply_request_no (request_no),
  UNIQUE KEY uk_venue_apply_active_key (active_unique_key),
  KEY idx_venue_apply_user_time (applicant_user_id, submitted_at),
  KEY idx_venue_apply_status_time (apply_status, submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场馆入驻申请';

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

CALL add_index_if_missing('ldqc_venue', 'uk_venue_source_apply', 'UNIQUE KEY `uk_venue_source_apply` (`source_apply_id`)');
CALL add_index_if_missing('ldqc_venue', 'idx_venue_owner_status', 'KEY `idx_venue_owner_status` (`owner_user_id`,`status`,`is_deleted`)');
DROP PROCEDURE IF EXISTS add_index_if_missing;
