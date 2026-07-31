-- 赛事报名订单与名额状态机升级
-- 适用：MySQL 5.7
-- 执行前请备份 ldqc_competition、ldqc_competition_signup。
-- 注意：历史报名迁移只处理尚无 order_no 的旧记录，避免重复执行时覆盖新流程订单状态。

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

CALL add_column_if_missing('ldqc_competition', 'signup_start_time', 'datetime DEFAULT NULL COMMENT ''报名开始时间''');
CALL add_column_if_missing('ldqc_competition', 'signup_end_time', 'datetime DEFAULT NULL COMMENT ''报名截止时间''');
CALL add_column_if_missing('ldqc_competition', 'max_people_per_order', 'int NOT NULL DEFAULT 1 COMMENT ''单个订单最大报名人数''');
CALL add_column_if_missing('ldqc_competition', 'payment_mode', 'varchar(16) NOT NULL DEFAULT ''FREE'' COMMENT ''支付方式 FREE/WECHAT''');
CALL add_column_if_missing('ldqc_competition', 'signup_notice', 'text NULL COMMENT ''报名须知与免责声明''');

CALL add_column_if_missing('ldqc_competition_signup', 'order_no', 'varchar(40) DEFAULT NULL COMMENT ''报名订单号''');
CALL add_column_if_missing('ldqc_competition_signup', 'request_id', 'varchar(64) DEFAULT NULL COMMENT ''客户端幂等请求号''');
CALL add_column_if_missing('ldqc_competition_signup', 'active_unique_key', 'varchar(80) DEFAULT NULL COMMENT ''活动订单唯一键，取消后置空''');
CALL add_column_if_missing('ldqc_competition_signup', 'competition_title', 'varchar(200) DEFAULT NULL COMMENT ''赛事标题快照''');
CALL add_column_if_missing('ldqc_competition_signup', 'competition_cover', 'varchar(1000) DEFAULT NULL COMMENT ''赛事封面快照''');
CALL add_column_if_missing('ldqc_competition_signup', 'competition_start_time', 'datetime DEFAULT NULL COMMENT ''赛事开始时间快照''');
CALL add_column_if_missing('ldqc_competition_signup', 'competition_end_time', 'datetime DEFAULT NULL COMMENT ''赛事结束时间快照''');
CALL add_column_if_missing('ldqc_competition_signup', 'competition_location', 'varchar(255) DEFAULT NULL COMMENT ''赛事地点快照''');
CALL add_column_if_missing('ldqc_competition_signup', 'competition_address', 'varchar(500) DEFAULT NULL COMMENT ''赛事地址快照''');
CALL add_column_if_missing('ldqc_competition_signup', 'unit_price', 'decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''单人费用快照''');
CALL add_column_if_missing('ldqc_competition_signup', 'total_amount', 'decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''订单应付金额''');
CALL add_column_if_missing('ldqc_competition_signup', 'payment_mode', 'varchar(16) NOT NULL DEFAULT ''FREE'' COMMENT ''FREE/WECHAT''');
CALL add_column_if_missing('ldqc_competition_signup', 'order_status', 'varchar(24) NOT NULL DEFAULT ''LEGACY_REVIEW'' COMMENT ''PENDING_PAYMENT/CONFIRMED/CANCELLED/EXPIRED/REFUND_PENDING/REFUNDED/LEGACY_REVIEW''');
CALL add_column_if_missing('ldqc_competition_signup', 'payment_order_no', 'varchar(64) DEFAULT NULL COMMENT ''第三方支付单号''');
CALL add_column_if_missing('ldqc_competition_signup', 'payment_expire_time', 'datetime DEFAULT NULL COMMENT ''待支付过期时间''');
CALL add_column_if_missing('ldqc_competition_signup', 'paid_at', 'datetime DEFAULT NULL COMMENT ''支付完成时间''');
CALL add_column_if_missing('ldqc_competition_signup', 'cancelled_at', 'datetime DEFAULT NULL COMMENT ''取消或关闭时间''');
CALL add_column_if_missing('ldqc_competition_signup', 'cancel_reason', 'varchar(500) DEFAULT NULL COMMENT ''取消、关闭或退款原因''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

UPDATE ldqc_competition
   SET signup_end_time = COALESCE(signup_end_time, start_time),
       max_people_per_order = CASE WHEN max_people_per_order IS NULL OR max_people_per_order <= 0 THEN 1 ELSE max_people_per_order END,
       participant_count = GREATEST(IFNULL(participant_count, 0), 0),
       payment_mode = CASE WHEN IFNULL(price, 0) <= 0 THEN 'FREE' ELSE 'WECHAT' END;

-- 旧流程允许前端传 pay_status，无法证明历史支付真实性，因此仅将尚未生成订单号的旧记录迁移为待运营核对。
UPDATE ldqc_competition_signup s
LEFT JOIN ldqc_competition c ON c.id = s.competition_id
   SET s.order_no = CONCAT('LEGACY-', s.id),
       s.request_id = COALESCE(NULLIF(s.request_id, ''), CONCAT('LEGACY-', s.id)),
       s.active_unique_key = NULL,
       s.competition_title = COALESCE(s.competition_title, c.title),
       s.competition_cover = COALESCE(s.competition_cover, c.cover_image),
       s.competition_start_time = COALESCE(s.competition_start_time, c.start_time),
       s.competition_end_time = COALESCE(s.competition_end_time, c.end_time),
       s.competition_location = COALESCE(s.competition_location, c.location),
       s.competition_address = COALESCE(s.competition_address, c.address),
       s.unit_price = COALESCE(s.unit_price, c.price, 0),
       s.total_amount = COALESCE(s.total_amount, COALESCE(c.price,0) * GREATEST(IFNULL(s.num_people,1),1)),
       s.payment_mode = CASE WHEN COALESCE(c.price,0) <= 0 THEN 'FREE' ELSE 'WECHAT' END,
       s.order_status = 'LEGACY_REVIEW'
 WHERE s.is_deleted = 0
   AND (s.order_no IS NULL OR s.order_no = '');

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

CALL add_index_if_missing('ldqc_competition_signup', 'uk_comp_signup_order_no', 'UNIQUE KEY `uk_comp_signup_order_no` (`order_no`)');
CALL add_index_if_missing('ldqc_competition_signup', 'uk_comp_signup_user_request', 'UNIQUE KEY `uk_comp_signup_user_request` (`user_id`,`request_id`,`is_deleted`)');
CALL add_index_if_missing('ldqc_competition_signup', 'uk_comp_signup_active_key', 'UNIQUE KEY `uk_comp_signup_active_key` (`active_unique_key`)');
CALL add_index_if_missing('ldqc_competition_signup', 'idx_comp_signup_expire', 'KEY `idx_comp_signup_expire` (`order_status`,`payment_expire_time`)');
CALL add_index_if_missing('ldqc_competition_signup', 'idx_comp_signup_user_time', 'KEY `idx_comp_signup_user_time` (`user_id`,`signup_time`)');
CALL add_index_if_missing('ldqc_competition', 'idx_comp_signup_window', 'KEY `idx_comp_signup_window` (`status`,`signup_start_time`,`signup_end_time`)');

DROP PROCEDURE IF EXISTS add_index_if_missing;
