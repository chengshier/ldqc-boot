-- 社区内容审核工作台菜单。
-- 页面组件：src/views/content/moderation.vue
-- 执行后请在角色管理中为运营人员授予查看、审核和下架按钮权限。

INSERT INTO blade_menu
(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES
('205160001', '0', 'contentModeration', '内容审核', 'menu', '/content/moderation', 'iconfont icon-shenhe', 16, 1, 0, 1, '社区图文与短视频审核工作台', 0),
('205160002', '205160001', 'contentModeration_view', '查看详情', 'view', '/content/moderation/view', 'file-text', 1, 2, 2, 1, '查看作者、文案和媒体内容', 0),
('205160003', '205160001', 'contentModeration_pass', '审核通过', 'pass', '/api/blade-imgDetail/imgDetail/audit', 'check', 2, 2, 2, 1, '通过后内容正式公开', 0),
('205160004', '205160001', 'contentModeration_reject', '审核驳回', 'reject', '/api/blade-imgDetail/imgDetail/audit', 'close', 3, 2, 2, 1, '驳回并通知发布者修改', 0),
('205160005', '205160001', 'contentModeration_offline', '内容下架', 'offline', '/api/blade-imgDetail/imgDetail/audit', 'delete', 4, 2, 2, 1, '下架已发布内容并保留原因', 0)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
path = VALUES(path),
source = VALUES(source),
sort = VALUES(sort),
remark = VALUES(remark),
is_deleted = 0;
