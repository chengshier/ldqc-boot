-- 社区内容审核与评论审核异常工作台菜单。
-- 页面组件：
--   src/views/content/moderation.vue
--   src/views/content/auditExceptions.vue
-- 执行后请在角色管理中为运营人员授予对应查看和处理权限。

INSERT INTO blade_menu
(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES
('205160001', '0', 'contentModeration', '内容审核', 'menu', '/content/moderation', 'iconfont icon-shenhe', 16, 1, 0, 1, '社区图文与短视频审核工作台', 0),
('205160002', '205160001', 'contentModeration_view', '查看详情', 'view', '/content/moderation/view', 'file-text', 1, 2, 2, 1, '查看作者、文案和媒体内容', 0),
('205160003', '205160001', 'contentModeration_pass', '审核通过', 'pass', '/api/blade-imgDetail/imgDetail/audit', 'check', 2, 2, 2, 1, '通过后内容正式公开', 0),
('205160004', '205160001', 'contentModeration_reject', '审核驳回', 'reject', '/api/blade-imgDetail/imgDetail/audit', 'close', 3, 2, 2, 1, '驳回并通知发布者修改', 0),
('205160005', '205160001', 'contentModeration_offline', '内容下架', 'offline', '/api/blade-imgDetail/imgDetail/audit', 'delete', 4, 2, 2, 1, '下架已发布内容并保留原因', 0),
('205160006', '0', 'contentAuditExceptions', '审核异常待办', 'menu', '/content/auditExceptions', 'iconfont icon-warning', 17, 1, 0, 1, '评论自动审核失败后的重试和人工处理工作台', 0),
('205160007', '205160006', 'contentAuditExceptions_retry', '立即重试', 'retry', '/api/blade-contentaudit/task/retry-now', 'refresh', 1, 2, 2, 1, '立即重新调用内容安全审核', 0),
('205160008', '205160006', 'contentAuditExceptions_resolve', '人工处理', 'resolve', '/api/blade-contentaudit/task/resolve', 'check', 2, 2, 2, 1, '人工通过或拒绝连续失败的评论审核任务', 0)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
path = VALUES(path),
source = VALUES(source),
sort = VALUES(sort),
remark = VALUES(remark),
is_deleted = 0;
