INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970549429997570', '2015967849355186177', 'albumImgRelation', '相册图片关系表', 'menu', '/albumimgrelation/albumImgRelation', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970549429997571', '2015970549429997570', 'albumImgRelation_add', '新增', 'add', '/albumimgrelation/albumImgRelation/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970549429997572', '2015970549429997570', 'albumImgRelation_edit', '修改', 'edit', '/albumimgrelation/albumImgRelation/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970549429997573', '2015970549429997570', 'albumImgRelation_delete', '删除', 'delete', '/api/blade-albumimgrelation/albumImgRelation/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970549429997574', '2015970549429997570', 'albumImgRelation_view', '查看', 'view', '/albumimgrelation/albumImgRelation/view', 'file-text', 4, 2, 2, 1, NULL, 0);
