INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633843682508801', '2015967849355186177', 'userAuthFile', '用户认证附件表', 'menu', '/userauthfile/userAuthFile', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633843682508802', '2039633843682508801', 'userAuthFile_add', '新增', 'add', '/userauthfile/userAuthFile/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633843682508803', '2039633843682508801', 'userAuthFile_edit', '修改', 'edit', '/userauthfile/userAuthFile/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633843682508804', '2039633843682508801', 'userAuthFile_delete', '删除', 'delete', '/api/blade-userauthfile/userAuthFile/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633843682508805', '2039633843682508801', 'userAuthFile_view', '查看', 'view', '/userauthfile/userAuthFile/view', 'file-text', 4, 2, 2, 1, NULL, 0);
