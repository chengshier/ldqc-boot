INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262314410221570', '2015967849355186177', 'venue', '体育场馆表', 'menu', '/venue/venue', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262314410221571', '2031262314410221570', 'venue_add', '新增', 'add', '/venue/venue/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262314410221572', '2031262314410221570', 'venue_edit', '修改', 'edit', '/venue/venue/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262314410221573', '2031262314410221570', 'venue_delete', '删除', 'delete', '/api/blade-venue/venue/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262314410221574', '2031262314410221570', 'venue_view', '查看', 'view', '/venue/venue/view', 'file-text', 4, 2, 2, 1, NULL, 0);
