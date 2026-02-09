INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970550918975490', '2015967849355186177', 'category', '分类表', 'menu', '/category/category', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970550918975491', '2015970550918975490', 'category_add', '新增', 'add', '/category/category/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970550918975492', '2015970550918975490', 'category_edit', '修改', 'edit', '/category/category/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970550918975493', '2015970550918975490', 'category_delete', '删除', 'delete', '/api/blade-category/category/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970550918975494', '2015970550918975490', 'category_view', '查看', 'view', '/category/category/view', 'file-text', 4, 2, 2, 1, NULL, 0);
