INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028312860889063426', '2028307934607712257', 'news', '新闻表', 'menu', '/news/news', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028312860889063427', '2028312860889063426', 'news_add', '新增', 'add', '/news/news/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028312860889063428', '2028312860889063426', 'news_edit', '修改', 'edit', '/news/news/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028312860889063429', '2028312860889063426', 'news_delete', '删除', 'delete', '/api/blade-news/news/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028312860889063430', '2028312860889063426', 'news_view', '查看', 'view', '/news/news/view', 'file-text', 4, 2, 2, 1, NULL, 0);
