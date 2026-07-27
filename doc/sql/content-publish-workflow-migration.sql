-- 绿动全城社区内容发布审核与达人联动迁移
-- 适用数据库：MySQL 5.7
-- 执行前：备份 t_img_detail、ldqc_talent_post。
-- 状态约定：t_img_detail.status 0待审核、1已发布、2审核拒绝、3已下架。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition TEXT
)
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

CALL add_column_if_missing('t_img_detail', 'audit_reason', 'varchar(500) DEFAULT NULL COMMENT ''审核拒绝或下架原因''');
CALL add_column_if_missing('t_img_detail', 'audit_time', 'datetime DEFAULT NULL COMMENT ''审核时间''');
CALL add_column_if_missing('t_img_detail', 'audit_user_id', 'bigint DEFAULT NULL COMMENT ''审核运营人员ID''');
CALL add_column_if_missing('t_img_detail', 'publish_time', 'datetime DEFAULT NULL COMMENT ''正式发布时间''');
CALL add_column_if_missing('t_img_detail', 'media_process_status', 'varchar(32) NOT NULL DEFAULT ''READY'' COMMENT ''媒体处理状态 PROCESSING/READY/FAILED''');
CALL add_column_if_missing('ldqc_talent_post', 'source_content_id', 'bigint DEFAULT NULL COMMENT ''来源社区内容ID''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

-- 索引采用 information_schema 判断，保证脚本可重复执行。
SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_img_detail' AND INDEX_NAME = 'idx_img_detail_public'),
    'SELECT 1',
    'CREATE INDEX idx_img_detail_public ON t_img_detail(status, is_deleted, publish_time)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_img_detail' AND INDEX_NAME = 'idx_img_detail_user_status'),
    'SELECT 1',
    'CREATE INDEX idx_img_detail_user_status ON t_img_detail(user_id, status, create_time)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ldqc_talent_post' AND INDEX_NAME = 'uk_talent_post_source_content'),
    'SELECT 1',
    'CREATE UNIQUE INDEX uk_talent_post_source_content ON ldqc_talent_post(source_content_id)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史数据兼容：原先 status=1 的内容视为已发布，并补齐发布时间。
UPDATE t_img_detail
   SET publish_time = COALESCE(publish_time, create_time),
       media_process_status = CASE
           WHEN UPPER(COALESCE(media_type, 'IMAGE')) = 'VIDEO'
                AND COALESCE(NULLIF(poster_url, ''), NULLIF(cover, '')) IS NULL
           THEN 'PROCESSING'
           ELSE 'READY'
       END
 WHERE status = 1
   AND is_deleted = 0;
