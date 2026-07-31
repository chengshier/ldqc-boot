-- 用户关注关系一致性升级
-- 适用：MySQL 5.7
-- 执行前请备份 t_follow、blade_user。

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

CALL add_column_if_missing('t_follow', 'active_unique_key', 'varchar(80) DEFAULT NULL COMMENT ''有效关注唯一键 uid:fid，取消后置空''');
DROP PROCEDURE IF EXISTS add_column_if_missing;

-- 删除自关注关系。
UPDATE t_follow
   SET is_deleted = 1,
       active_unique_key = NULL
 WHERE is_deleted = 0
   AND uid = fid;

-- 同一用户对同一目标存在多条有效关系时，只保留ID最小的一条。
UPDATE t_follow duplicate_row
JOIN t_follow keep_row
  ON keep_row.uid = duplicate_row.uid
 AND keep_row.fid = duplicate_row.fid
 AND keep_row.is_deleted = 0
 AND duplicate_row.is_deleted = 0
 AND keep_row.id < duplicate_row.id
   SET duplicate_row.is_deleted = 1,
       duplicate_row.active_unique_key = NULL;

UPDATE t_follow
   SET active_unique_key = CASE WHEN is_deleted = 0 THEN CONCAT(uid, ':', fid) ELSE NULL END;

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

CALL add_index_if_missing('t_follow', 'uk_follow_active_key', 'UNIQUE KEY `uk_follow_active_key` (`active_unique_key`)');
CALL add_index_if_missing('t_follow', 'idx_follow_uid_time', 'KEY `idx_follow_uid_time` (`uid`,`is_deleted`,`create_time`)');
CALL add_index_if_missing('t_follow', 'idx_follow_fid_time', 'KEY `idx_follow_fid_time` (`fid`,`is_deleted`,`create_time`)');
DROP PROCEDURE IF EXISTS add_index_if_missing;

-- 以有效关注关系重新校准用户统计，消除历史重复关系和计数漂移。
UPDATE blade_user user_row
LEFT JOIN (
    SELECT uid, COUNT(*) AS follow_count
      FROM t_follow
     WHERE is_deleted = 0
     GROUP BY uid
) following_stat ON following_stat.uid = user_row.id
LEFT JOIN (
    SELECT fid, COUNT(*) AS fan_count
      FROM t_follow
     WHERE is_deleted = 0
     GROUP BY fid
) fan_stat ON fan_stat.fid = user_row.id
   SET user_row.follow_count = IFNULL(following_stat.follow_count, 0),
       user_row.fan_count = IFNULL(fan_stat.fan_count, 0)
 WHERE user_row.is_deleted = 0;
