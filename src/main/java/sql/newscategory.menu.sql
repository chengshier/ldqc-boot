INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350551395553282', '2028307934607712257', 'newsCategory', '新闻分类表', 'menu', '/newscategory/newsCategory', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350551395553283', '2028350551395553282', 'newsCategory_add', '新增', 'add', '/newscategory/newsCategory/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350551395553284', '2028350551395553282', 'newsCategory_edit', '修改', 'edit', '/newscategory/newsCategory/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350551395553285', '2028350551395553282', 'newsCategory_delete', '删除', 'delete', '/api/blade-newscategory/newsCategory/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350551395553286', '2028350551395553282', 'newsCategory_view', '查看', 'view', '/newscategory/newsCategory/view', 'file-text', 4, 2, 2, 1, NULL, 0);
