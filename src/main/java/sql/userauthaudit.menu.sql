INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633841878958082', '2015967849355186177', 'userAuthAudit', '用户认证审核日志表', 'menu', '/userauthaudit/userAuthAudit', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633841878958083', '2039633841878958082', 'userAuthAudit_add', '新增', 'add', '/userauthaudit/userAuthAudit/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633841878958084', '2039633841878958082', 'userAuthAudit_edit', '修改', 'edit', '/userauthaudit/userAuthAudit/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633841878958085', '2039633841878958082', 'userAuthAudit_delete', '删除', 'delete', '/api/blade-userauthaudit/userAuthAudit/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633841878958086', '2039633841878958082', 'userAuthAudit_view', '查看', 'view', '/userauthaudit/userAuthAudit/view', 'file-text', 4, 2, 2, 1, NULL, 0);
