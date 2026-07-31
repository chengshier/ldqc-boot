-- 体育培训长视频课程底座
-- 适用数据库：MySQL 5.7
-- 现有 ldqc_training 继续作为课程主表；新增章节、课时、访问授权和学习进度。

SET NAMES utf8mb4;

-- 记录字段是否在本次迁移前已经存在，避免脚本重复执行时重新发布运营人员创建的草稿。
SET @publish_status_existed_before = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ldqc_training' AND COLUMN_NAME = 'publish_status'
);

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

CALL add_column_if_missing('ldqc_training', 'content_mode', 'varchar(16) NOT NULL DEFAULT ''OFFLINE'' COMMENT ''课程形态 OFFLINE/ONLINE/MIXED''');
CALL add_column_if_missing('ldqc_training', 'publish_status', 'varchar(16) NOT NULL DEFAULT ''DRAFT'' COMMENT ''发布状态 DRAFT/PENDING/PUBLISHED/REJECTED/OFFLINE''');
CALL add_column_if_missing('ldqc_training', 'talent_user_id', 'bigint DEFAULT NULL COMMENT ''达人课程所属用户，平台课程为空''');
CALL add_column_if_missing('ldqc_training', 'purchase_required', 'tinyint NOT NULL DEFAULT 0 COMMENT ''是否需要购买或授权 0否1是''');
CALL add_column_if_missing('ldqc_training', 'total_lessons', 'int NOT NULL DEFAULT 0 COMMENT ''课时总数''');
CALL add_column_if_missing('ldqc_training', 'total_video_duration', 'int NOT NULL DEFAULT 0 COMMENT ''视频总时长秒''');
CALL add_column_if_missing('ldqc_training', 'audit_reason', 'varchar(500) DEFAULT NULL COMMENT ''课程审核说明''');

DROP PROCEDURE IF EXISTS add_column_if_missing;

CREATE TABLE IF NOT EXISTS ldqc_training_chapter (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000',
  training_id bigint NOT NULL COMMENT '课程ID',
  title varchar(150) NOT NULL COMMENT '章节标题',
  description varchar(500) DEFAULT NULL COMMENT '章节说明',
  sort_order int NOT NULL DEFAULT 0,
  status int NOT NULL DEFAULT 1,
  create_user bigint DEFAULT NULL,
  create_dept bigint DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_user bigint DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted int NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_training_chapter_course_sort (training_id, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训课程章节';

CREATE TABLE IF NOT EXISTS ldqc_training_lesson (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000',
  training_id bigint NOT NULL COMMENT '课程ID',
  chapter_id bigint NOT NULL COMMENT '章节ID',
  title varchar(150) NOT NULL COMMENT '课时标题',
  lesson_type varchar(16) NOT NULL DEFAULT 'VIDEO' COMMENT 'VIDEO/TEXT',
  video_url varchar(1000) DEFAULT NULL COMMENT '原始或转码后视频地址',
  poster_url varchar(1000) DEFAULT NULL COMMENT '视频封面',
  duration_seconds int NOT NULL DEFAULT 0 COMMENT '视频时长秒',
  is_trial tinyint NOT NULL DEFAULT 0 COMMENT '是否免费试看',
  media_process_status varchar(32) NOT NULL DEFAULT 'READY' COMMENT 'UPLOADING/PROCESSING/READY/FAILED',
  sort_order int NOT NULL DEFAULT 0,
  status int NOT NULL DEFAULT 1,
  create_user bigint DEFAULT NULL,
  create_dept bigint DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_user bigint DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted int NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_training_lesson_course_sort (training_id, status, sort_order),
  KEY idx_training_lesson_chapter_sort (chapter_id, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训课程课时';

CREATE TABLE IF NOT EXISTS ldqc_training_access (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000',
  user_id bigint NOT NULL COMMENT '用户ID',
  training_id bigint NOT NULL COMMENT '课程ID',
  source_type varchar(24) NOT NULL DEFAULT 'ADMIN' COMMENT 'FREE/ORDER/ADMIN/ACTIVITY',
  source_id varchar(64) DEFAULT NULL COMMENT '订单或授权来源ID',
  access_status varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/REVOKED/EXPIRED',
  valid_start_at datetime DEFAULT NULL,
  valid_end_at datetime DEFAULT NULL,
  status int NOT NULL DEFAULT 1,
  create_user bigint DEFAULT NULL,
  create_dept bigint DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_user bigint DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted int NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_training_access_user_course (user_id, training_id, is_deleted),
  KEY idx_training_access_valid (access_status, valid_end_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训课程播放授权';

CREATE TABLE IF NOT EXISTS ldqc_training_progress (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000',
  user_id bigint NOT NULL COMMENT '用户ID',
  training_id bigint NOT NULL COMMENT '课程ID',
  lesson_id bigint NOT NULL COMMENT '课时ID',
  progress_seconds int NOT NULL DEFAULT 0 COMMENT '已播放秒数',
  duration_seconds int NOT NULL DEFAULT 0 COMMENT '课时时长快照',
  completed tinyint NOT NULL DEFAULT 0 COMMENT '是否学完',
  last_play_at datetime DEFAULT NULL COMMENT '最近播放时间',
  status int NOT NULL DEFAULT 1,
  create_user bigint DEFAULT NULL,
  create_dept bigint DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_user bigint DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted int NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_training_progress_user_lesson (user_id, lesson_id, is_deleted),
  KEY idx_training_progress_course (user_id, training_id, last_play_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训课程学习进度';

-- 仅首次新增 publish_status 字段时迁移历史数据；重复执行不改变后续运营草稿状态。
UPDATE ldqc_training
   SET content_mode = COALESCE(NULLIF(content_mode, ''), 'OFFLINE'),
       publish_status = CASE WHEN status = 1 THEN 'PUBLISHED' ELSE 'OFFLINE' END
 WHERE @publish_status_existed_before = 0;
