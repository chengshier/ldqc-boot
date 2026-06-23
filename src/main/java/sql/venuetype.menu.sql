INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541331995348994', '2015967849355186177', 'venueType', '场馆类型表', 'menu', '/venuetype/venueType', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541331995348995', '2039541331995348994', 'venueType_add', '新增', 'add', '/venuetype/venueType/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541331995348996', '2039541331995348994', 'venueType_edit', '修改', 'edit', '/venuetype/venueType/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541331995348997', '2039541331995348994', 'venueType_delete', '删除', 'delete', '/api/blade-venuetype/venueType/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541331995348998', '2039541331995348994', 'venueType_view', '查看', 'view', '/venuetype/venueType/view', 'file-text', 4, 2, 2, 1, NULL, 0);
