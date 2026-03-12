INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350654646734850', '2028307934607712257', 'newsImages', '新闻图片表', 'menu', '/newsimages/newsImages', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350654646734851', '2028350654646734850', 'newsImages_add', '新增', 'add', '/newsimages/newsImages/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350654646734852', '2028350654646734850', 'newsImages_edit', '修改', 'edit', '/newsimages/newsImages/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350654646734853', '2028350654646734850', 'newsImages_delete', '删除', 'delete', '/api/blade-newsimages/newsImages/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350654646734854', '2028350654646734850', 'newsImages_view', '查看', 'view', '/newsimages/newsImages/view', 'file-text', 4, 2, 2, 1, NULL, 0);
