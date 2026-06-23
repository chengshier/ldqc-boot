INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541338886590466', '2015967849355186177', 'outdoorMedia', '户外图集表', 'menu', '/outdoormedia/outdoorMedia', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541338886590467', '2039541338886590466', 'outdoorMedia_add', '新增', 'add', '/outdoormedia/outdoorMedia/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541338886590468', '2039541338886590466', 'outdoorMedia_edit', '修改', 'edit', '/outdoormedia/outdoorMedia/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541338886590469', '2039541338886590466', 'outdoorMedia_delete', '删除', 'delete', '/api/blade-outdoormedia/outdoorMedia/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541338886590470', '2039541338886590466', 'outdoorMedia_view', '查看', 'view', '/outdoormedia/outdoorMedia/view', 'file-text', 4, 2, 2, 1, NULL, 0);
