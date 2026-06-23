-- 增量：成长等级 + 券模板 + 核销日志
ALTER TABLE points_account ADD COLUMN growth_level INT NOT NULL DEFAULT 0 COMMENT '成长等级(基于累计获得绿豆)' AFTER total_spent_points;

CREATE TABLE IF NOT EXISTS growth_level_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  level_no INT NOT NULL,
  level_name VARCHAR(64) NOT NULL,
  min_earned_points INT NOT NULL,
  icon_url VARCHAR(255) DEFAULT NULL,
  privilege_desc VARCHAR(255) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user BIGINT DEFAULT NULL,
  update_user BIGINT DEFAULT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  tenant_id VARCHAR(32) DEFAULT '000000'
);

ALTER TABLE coupon_template ADD COLUMN min_growth_level INT NOT NULL DEFAULT 0 COMMENT '领取所需最低成长等级';

CREATE TABLE IF NOT EXISTS coupon_verify_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_coupon_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  merchant_user_id BIGINT NOT NULL,
  template_id BIGINT DEFAULT NULL,
  coupon_no VARCHAR(64) NOT NULL,
  verify_channel VARCHAR(32) DEFAULT 'APP',
  verify_result TINYINT NOT NULL DEFAULT 1,
  fail_reason VARCHAR(255) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  verify_status VARCHAR(32) NOT NULL DEFAULT 'PROCESSING' COMMENT '状态 PROCESSING/FINISHED',
  order_no VARCHAR(64) DEFAULT NULL,
  ext_json TEXT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user BIGINT DEFAULT NULL,
  update_user BIGINT DEFAULT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  tenant_id VARCHAR(32) DEFAULT '000000'
);

ALTER TABLE user_coupon ADD COLUMN verify_merchant_user_id BIGINT DEFAULT NULL COMMENT '核销商家用户ID' AFTER used_at;

INSERT INTO growth_level_config(level_no, level_name, min_earned_points, privilege_desc, status)
VALUES
(1, '成长1级', 100, '可领取基础成长券', 1),
(2, '成长2级', 1000, '可领取2级专属券', 1),
(3, '成长3级', 3000, '可领取3级专属券', 1),
(4, '成长4级', 8000, '可领取高阶专属券', 1),
(5, '成长5级', 20000, '可领取顶级专属券', 1);
