-- 评论内容审核：执行前请先备份 t_comment 与 n_news_comment。

ALTER TABLE t_comment
  ADD COLUMN audit_status tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2未通过 3异常待重试',
  ADD COLUMN audit_reason varchar(500) DEFAULT NULL COMMENT '审核说明',
  ADD COLUMN audit_time datetime DEFAULT NULL COMMENT '审核完成时间',
  ADD COLUMN audit_task_id bigint DEFAULT NULL COMMENT '审核任务ID',
  ADD KEY idx_comment_public (mid, audit_status, is_deleted, create_time),
  ADD KEY idx_comment_user_audit (uid, audit_status, create_time);

ALTER TABLE n_news_comment
  ADD COLUMN audit_reason varchar(500) DEFAULT NULL COMMENT '审核说明',
  ADD COLUMN audit_time datetime DEFAULT NULL COMMENT '审核完成时间',
  ADD COLUMN audit_task_id bigint DEFAULT NULL COMMENT '审核任务ID',
  ADD KEY idx_news_comment_public (news_id, comment_status, is_deleted, create_time),
  ADD KEY idx_news_comment_user_audit (user_id, comment_status, create_time);

CREATE TABLE content_audit_task (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000' COMMENT '租户ID',
  biz_type varchar(32) NOT NULL COMMENT '业务类型：TREND_COMMENT/NEWS_COMMENT',
  biz_id bigint NOT NULL COMMENT '评论主键',
  user_id bigint NOT NULL COMMENT '发布用户',
  open_id varchar(128) DEFAULT NULL COMMENT '微信OpenID',
  content_snapshot varchar(2000) NOT NULL COMMENT '待审核文本快照',
  audit_status tinyint NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2未通过 3异常待重试',
  provider_trace_id varchar(128) DEFAULT NULL COMMENT '微信追踪标识',
  result_code varchar(64) DEFAULT NULL COMMENT '审核结果码',
  result_message varchar(500) DEFAULT NULL COMMENT '审核结果说明',
  attempt_count int NOT NULL DEFAULT 0 COMMENT '调用次数',
  next_retry_time datetime DEFAULT NULL COMMENT '下次重试时间',
  audit_time datetime DEFAULT NULL COMMENT '审核完成时间',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_audit_biz (biz_type, biz_id),
  KEY idx_audit_retry (audit_status, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容安全审核任务';

CREATE TABLE user_message (
  id bigint NOT NULL COMMENT '主键',
  tenant_id varchar(12) NOT NULL DEFAULT '000000' COMMENT '租户ID',
  user_id bigint NOT NULL COMMENT '接收用户',
  message_type varchar(32) NOT NULL COMMENT 'COMMENT_AUDIT_PASS/COMMENT_AUDIT_REJECT',
  title varchar(100) NOT NULL,
  content varchar(1000) NOT NULL,
  biz_type varchar(32) DEFAULT NULL,
  biz_id bigint DEFAULT NULL,
  extra_json varchar(1000) DEFAULT NULL COMMENT '跳转参数',
  read_status tinyint NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_user_message (user_id, read_status, create_time),
  UNIQUE KEY uk_message_biz_type (user_id, message_type, biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户站内消息';

-- 历史评论在迁移后默认视为已通过，避免已有公开内容全部消失。
UPDATE t_comment SET audit_status = 1 WHERE audit_status = 0;
UPDATE n_news_comment SET comment_status = 1 WHERE comment_status IS NULL OR comment_status = 0;
