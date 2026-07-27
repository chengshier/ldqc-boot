-- 培训课程运营菜单
-- 页面：
--   src/views/training/courseManager.vue
--   src/views/training/courseWorkbench.vue
-- 执行后请在角色管理中授予课程运营人员所需按钮权限。

INSERT INTO blade_menu
(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES
('205170000', '0', 'trainingOperation', '培训运营', 'menu', '/training', 'iconfont icon-peixun', 17, 1, 0, 1, '课程资料、章节课时和播放授权运营入口', 0),
('205170001', '205170000', 'trainingCourseManager', '课程管理', 'menu', '/training/course-manager', 'iconfont icon-kecheng', 1, 1, 0, 1, '维护课程基础资料和发布状态', 0),
('205170002', '205170000', 'trainingCourseWorkbench', '课程内容工作台', 'menu', '/training/course-workbench', 'iconfont icon-shipin', 2, 1, 0, 1, '维护章节、课时、试看、视频状态和用户授权', 0),
('205170011', '205170001', 'trainingCourse_edit', '编辑课程', 'edit', '/api/blade-training/course-admin/settings', 'edit', 1, 2, 2, 1, '新建或修改课程基础资料', 0),
('205170021', '205170002', 'trainingChapter_edit', '维护章节', 'chapter', '/api/blade-training/course-admin/chapter/save', 'list', 1, 2, 2, 1, '新增、修改和删除章节', 0),
('205170022', '205170002', 'trainingLesson_edit', '维护课时', 'lesson', '/api/blade-training/course-admin/lesson/save', 'video', 2, 2, 2, 1, '上传视频并维护课时', 0),
('205170023', '205170002', 'trainingCourse_publish', '发布课程', 'publish', '/api/blade-training/course-admin/publish', 'check', 3, 2, 2, 1, '检查课程完整性并发布', 0),
('205170024', '205170002', 'trainingCourse_offline', '下架课程', 'offline', '/api/blade-training/course-admin/offline', 'close', 4, 2, 2, 1, '填写原因后下架课程', 0),
('205170025', '205170002', 'trainingAccess_manage', '播放授权', 'access', '/api/blade-training/course-admin/access/grant', 'user', 5, 2, 2, 1, '搜索用户并授予或撤销播放权限', 0)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
path = VALUES(path),
source = VALUES(source),
sort = VALUES(sort),
remark = VALUES(remark),
is_deleted = 0;
