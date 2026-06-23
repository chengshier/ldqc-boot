INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541337145954306', '2015967849355186177', 'outdoorSignup', '户外报名表', 'menu', '/outdoorsignup/outdoorSignup', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541337145954307', '2039541337145954306', 'outdoorSignup_add', '新增', 'add', '/outdoorsignup/outdoorSignup/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541337145954308', '2039541337145954306', 'outdoorSignup_edit', '修改', 'edit', '/outdoorsignup/outdoorSignup/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541337145954309', '2039541337145954306', 'outdoorSignup_delete', '删除', 'delete', '/api/blade-outdoorsignup/outdoorSignup/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541337145954310', '2039541337145954306', 'outdoorSignup_view', '查看', 'view', '/outdoorsignup/outdoorSignup/view', 'file-text', 4, 2, 2, 1, NULL, 0);
