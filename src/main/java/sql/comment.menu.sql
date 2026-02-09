INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970552391176193', '2015967849355186177', 'comment', '评论表', 'menu', '/comment/comment', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970552391176194', '2015970552391176193', 'comment_add', '新增', 'add', '/comment/comment/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970552391176195', '2015970552391176193', 'comment_edit', '修改', 'edit', '/comment/comment/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970552391176196', '2015970552391176193', 'comment_delete', '删除', 'delete', '/api/blade-comment/comment/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970552391176197', '2015970552391176193', 'comment_view', '查看', 'view', '/comment/comment/view', 'file-text', 4, 2, 2, 1, NULL, 0);
