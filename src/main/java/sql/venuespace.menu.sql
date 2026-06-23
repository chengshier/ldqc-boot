INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541333605961729', '2015967849355186177', 'venueSpace', '场馆场地表', 'menu', '/venuespace/venueSpace', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541333605961730', '2039541333605961729', 'venueSpace_add', '新增', 'add', '/venuespace/venueSpace/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541333605961731', '2039541333605961729', 'venueSpace_edit', '修改', 'edit', '/venuespace/venueSpace/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541333605961732', '2039541333605961729', 'venueSpace_delete', '删除', 'delete', '/api/blade-venuespace/venueSpace/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541333605961733', '2039541333605961729', 'venueSpace_view', '查看', 'view', '/venuespace/venueSpace/view', 'file-text', 4, 2, 2, 1, NULL, 0);
