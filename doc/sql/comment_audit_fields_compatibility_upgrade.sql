-- 社区评论与新闻评论审核字段兼容升级
-- 适用：MySQL 5.7
-- 目的：保证微信文本审核、自动重试和人工复核代码依赖的字段真实存在。
-- 历史评论默认初始化为已通过（1），避免升级后现有评论全部不可见。

SET NAMES utf8mb4;
SET @schema_name = DATABASE();

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_definition TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = @schema_name
          AND table_name = p_table_name
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = @schema_name
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @sql_text = CONCAT(
            'ALTER TABLE `', p_table_name,
            '` ADD COLUMN `', p_column_name, '` ', p_definition
        );
        PREPARE statement_to_run FROM @sql_text;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE add_index_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_index_name VARCHAR(128),
    IN p_index_columns VARCHAR(1000)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = @schema_name
          AND table_name = p_table_name
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = @schema_name
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @sql_text = CONCAT(
            'ALTER TABLE `', p_table_name,
            '` ADD INDEX `', p_index_name, '` (', p_index_columns, ')'
        );
        PREPARE statement_to_run FROM @sql_text;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

-- 社区动态评论 t_comment
CALL add_column_if_missing(
    't_comment',
    'audit_status',
    'tinyint(4) NOT NULL DEFAULT 1 COMMENT ''审核状态：0处理中 1通过 2拒绝 3重试中 4待人工'''
);
CALL add_column_if_missing(
    't_comment',
    'audit_reason',
    'varchar(500) DEFAULT NULL COMMENT ''审核结果或人工处理说明'''
);
CALL add_column_if_missing(
    't_comment',
    'audit_time',
    'datetime DEFAULT NULL COMMENT ''最终审核时间'''
);
CALL add_column_if_missing(
    't_comment',
    'audit_task_id',
    'bigint(20) DEFAULT NULL COMMENT ''关联内容安全审核任务ID'''
);

-- 新闻评论 n_news_comment
CALL add_column_if_missing(
    'n_news_comment',
    'comment_status',
    'tinyint(4) NOT NULL DEFAULT 1 COMMENT ''审核状态：0处理中 1通过 2拒绝 3重试中 4待人工'''
);
CALL add_column_if_missing(
    'n_news_comment',
    'audit_reason',
    'varchar(500) DEFAULT NULL COMMENT ''审核结果或人工处理说明'''
);
CALL add_column_if_missing(
    'n_news_comment',
    'audit_time',
    'datetime DEFAULT NULL COMMENT ''最终审核时间'''
);
CALL add_column_if_missing(
    'n_news_comment',
    'audit_task_id',
    'bigint(20) DEFAULT NULL COMMENT ''关联内容安全审核任务ID'''
);

-- 历史评论兼容：空状态按已通过处理，保持升级前可见性。
UPDATE t_comment
SET audit_status = 1
WHERE audit_status IS NULL
  AND is_deleted = 0;

UPDATE n_news_comment
SET comment_status = 1
WHERE comment_status IS NULL
  AND is_deleted = 0;

-- 公开评论查询与审核任务回查索引。
CALL add_index_if_missing(
    't_comment',
    'idx_comment_public',
    '`mid`, `audit_status`, `is_deleted`, `create_time`'
);
CALL add_index_if_missing(
    't_comment',
    'idx_comment_audit_task',
    '`audit_task_id`'
);
CALL add_index_if_missing(
    'n_news_comment',
    'idx_news_comment_public',
    '`news_id`, `comment_status`, `is_deleted`, `create_time`'
);
CALL add_index_if_missing(
    'n_news_comment',
    'idx_news_comment_audit_task',
    '`audit_task_id`'
);

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

-- 验证字段
SELECT table_name, column_name, column_type, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
    (table_name = 't_comment' AND column_name IN (
      'audit_status', 'audit_reason', 'audit_time', 'audit_task_id'
    ))
    OR
    (table_name = 'n_news_comment' AND column_name IN (
      'comment_status', 'audit_reason', 'audit_time', 'audit_task_id'
    ))
  )
ORDER BY table_name, ordinal_position;
