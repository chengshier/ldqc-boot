INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633845490253825', '2015967849355186177', 'userAuthType', '用户认证类型表', 'menu', '/userauthtype/userAuthType', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633845490253826', '2039633845490253825', 'userAuthType_add', '新增', 'add', '/userauthtype/userAuthType/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633845490253827', '2039633845490253825', 'userAuthType_edit', '修改', 'edit', '/userauthtype/userAuthType/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633845490253828', '2039633845490253825', 'userAuthType_delete', '删除', 'delete', '/api/blade-userauthtype/userAuthType/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633845490253829', '2039633845490253825', 'userAuthType_view', '查看', 'view', '/userauthtype/userAuthType/view', 'file-text', 4, 2, 2, 1, NULL, 0);
