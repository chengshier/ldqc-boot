-- 推荐行为反馈表
-- 适用：MySQL 5.7

CREATE TABLE IF NOT EXISTS ldqc_recommend_feedback (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000',
  request_id varchar(64) NOT NULL COMMENT '用户级幂等请求号',
  user_id bigint NOT NULL COMMENT '用户ID',
  session_id varchar(64) DEFAULT NULL COMMENT '推荐会话ID',
  content_type varchar(16) NOT NULL COMMENT 'CONTENT/NEWS',
  content_id bigint NOT NULL COMMENT '内容ID',
  event_type varchar(24) NOT NULL COMMENT 'IMPRESSION/CLICK/DWELL/VIDEO_COMPLETE/NOT_INTERESTED',
  duration_ms bigint NOT NULL DEFAULT 0 COMMENT '停留或播放时长毫秒',
  extra_json varchar(2000) DEFAULT NULL COMMENT '扩展信息',
  occurred_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  status int NOT NULL DEFAULT 1,
  create_user bigint DEFAULT NULL,
  create_dept bigint DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_user bigint DEFAULT NULL,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted int NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_recommend_feedback_user_request (user_id, request_id, is_deleted),
  KEY idx_recommend_feedback_user_time (user_id, occurred_at),
  KEY idx_recommend_feedback_content_event (content_type, content_id, event_type, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐行为反馈';
