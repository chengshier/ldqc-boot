INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970559957700610', '2015967849355186177', 'tag', '标签表', 'menu', '/tag/tag', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970559957700611', '2015970559957700610', 'tag_add', '新增', 'add', '/tag/tag/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970559957700612', '2015970559957700610', 'tag_edit', '修改', 'edit', '/tag/tag/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970559957700613', '2015970559957700610', 'tag_delete', '删除', 'delete', '/api/blade-tag/tag/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970559957700614', '2015970559957700610', 'tag_view', '查看', 'view', '/tag/tag/view', 'file-text', 4, 2, 2, 1, NULL, 0);
