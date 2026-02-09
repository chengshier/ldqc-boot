INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970547890688002', '2015967849355186177', 'album', '相册表', 'menu', '/album/album', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970547890688003', '2015970547890688002', 'album_add', '新增', 'add', '/album/album/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970547890688004', '2015970547890688002', 'album_edit', '修改', 'edit', '/album/album/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970547890688005', '2015970547890688002', 'album_delete', '删除', 'delete', '/api/blade-album/album/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970547890688006', '2015970547890688002', 'album_view', '查看', 'view', '/album/album/view', 'file-text', 4, 2, 2, 1, NULL, 0);
