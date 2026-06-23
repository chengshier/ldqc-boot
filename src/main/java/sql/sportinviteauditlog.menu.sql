INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354346145325058', '2015967849355186177', 'sportInviteAuditLog', '运动邀约审核日志表', 'menu', '/sportinviteauditlog/sportInviteAuditLog', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354346145325059', '2057354346145325058', 'sportInviteAuditLog_add', '新增', 'add', '/sportinviteauditlog/sportInviteAuditLog/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354346145325060', '2057354346145325058', 'sportInviteAuditLog_edit', '修改', 'edit', '/sportinviteauditlog/sportInviteAuditLog/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354346145325061', '2057354346145325058', 'sportInviteAuditLog_delete', '删除', 'delete', '/api/blade-sportinviteauditlog/sportInviteAuditLog/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354346145325062', '2057354346145325058', 'sportInviteAuditLog_view', '查看', 'view', '/sportinviteauditlog/sportInviteAuditLog/view', 'file-text', 4, 2, 2, 1, NULL, 0);
