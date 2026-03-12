INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350609746710530', '2028307934607712257', 'newsComment', '新闻评论表', 'menu', '/newscomment/newsComment', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350609746710531', '2028350609746710530', 'newsComment_add', '新增', 'add', '/newscomment/newsComment/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350609746710532', '2028350609746710530', 'newsComment_edit', '修改', 'edit', '/newscomment/newsComment/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350609746710533', '2028350609746710530', 'newsComment_delete', '删除', 'delete', '/api/blade-newscomment/newsComment/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350609746710534', '2028350609746710530', 'newsComment_view', '查看', 'view', '/newscomment/newsComment/view', 'file-text', 4, 2, 2, 1, NULL, 0);
