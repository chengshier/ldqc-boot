-- 绿动全城：社区动态微信自动审核升级
-- 适用：MySQL 5.7
-- 前置：已执行 content_audit_retry_upgrade.sql 与 content-publish-workflow-migration.sql
-- 本脚本不创建收费服务配置，也不存储微信 app-secret 或回调 token。

SET @schema_name = DATABASE();

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
        WHERE table_schema = @schema_name AND table_name = p_table_name
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = @schema_name
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @sql_text = CONCAT(
            'ALTER TABLE `', p_table_name, '` ADD INDEX `', p_index_name, '` (', p_index_columns, ')'
        );
        PREPARE statement_to_run FROM @sql_text;
        EXECUTE statement_to_run;
        DEALLOCATE PREPARE statement_to_run;
    END IF;
END$$
DELIMITER ;

-- 同一条动态的文案和多个媒体任务聚合，以及运营异常列表查询。
CALL add_index_if_missing(
    'content_audit_task',
    'idx_content_audit_biz_status',
    '`biz_type`, `biz_id`, `audit_status`, `is_deleted`'
);

-- 微信媒体异步回调通过 trace_id 精确定位审核任务。
CALL add_index_if_missing(
    'content_audit_task',
    'idx_content_audit_provider_trace',
    '`provider_trace_id`, `is_deleted`'
);

DROP PROCEDURE IF EXISTS add_index_if_missing;

-- 业务类型约定：
-- IMG_DETAIL_TEXT  动态文案同步审核
-- IMG_DETAIL_MEDIA 动态图片或短视频封面异步审核
-- TREND_COMMENT    社区评论文本审核
-- NEWS_COMMENT     新闻评论文本审核
--
-- 审核状态约定：
-- 0 PROCESSING       调用中或等待微信媒体回调
-- 1 PASSED           自动或人工通过
-- 2 REJECTED         自动或人工拒绝
-- 3 RETRY            接口异常或回调超时，等待自动重试
-- 4 MANUAL_REQUIRED  微信建议复核或重试耗尽，等待运营人员处理
