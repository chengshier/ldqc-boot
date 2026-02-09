INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2018888096543756289', '2015967849355186177', 'userThree', '用户微信登录认证表', 'menu', '/userthree/userThree', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2018888096543756290', '2018888096543756289', 'userThree_add', '新增', 'add', '/userthree/userThree/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2018888096543756291', '2018888096543756289', 'userThree_edit', '修改', 'edit', '/userthree/userThree/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2018888096543756292', '2018888096543756289', 'userThree_delete', '删除', 'delete', '/api/blade-userthree/userThree/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2018888096543756293', '2018888096543756289', 'userThree_view', '查看', 'view', '/userthree/userThree/view', 'file-text', 4, 2, 2, 1, NULL, 0);
