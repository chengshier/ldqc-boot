INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970553867571201', '2015967849355186177', 'follow', '关注表', 'menu', '/follow/follow', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970553867571202', '2015970553867571201', 'follow_add', '新增', 'add', '/follow/follow/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970553867571203', '2015970553867571201', 'follow_edit', '修改', 'edit', '/follow/follow/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970553867571204', '2015970553867571201', 'follow_delete', '删除', 'delete', '/api/blade-follow/follow/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970553867571205', '2015970553867571201', 'follow_view', '查看', 'view', '/follow/follow/view', 'file-text', 4, 2, 2, 1, NULL, 0);
