INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970561627033602', '2015967849355186177', 'tagImgRelation', '标签图片关系表', 'menu', '/tagimgrelation/tagImgRelation', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970561627033603', '2015970561627033602', 'tagImgRelation_add', '新增', 'add', '/tagimgrelation/tagImgRelation/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970561627033604', '2015970561627033602', 'tagImgRelation_edit', '修改', 'edit', '/tagimgrelation/tagImgRelation/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970561627033605', '2015970561627033602', 'tagImgRelation_delete', '删除', 'delete', '/api/blade-tagimgrelation/tagImgRelation/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970561627033606', '2015970561627033602', 'tagImgRelation_view', '查看', 'view', '/tagimgrelation/tagImgRelation/view', 'file-text', 4, 2, 2, 1, NULL, 0);
