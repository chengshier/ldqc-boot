INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541326567919618', '2015967849355186177', 'competitionSignup', '赛事报名表', 'menu', '/competitionsignup/competitionSignup', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541326567919619', '2039541326567919618', 'competitionSignup_add', '新增', 'add', '/competitionsignup/competitionSignup/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541326567919620', '2039541326567919618', 'competitionSignup_edit', '修改', 'edit', '/competitionsignup/competitionSignup/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541326567919621', '2039541326567919618', 'competitionSignup_delete', '删除', 'delete', '/api/blade-competitionsignup/competitionSignup/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541326567919622', '2039541326567919618', 'competitionSignup_view', '查看', 'view', '/competitionsignup/competitionSignup/view', 'file-text', 4, 2, 2, 1, NULL, 0);
