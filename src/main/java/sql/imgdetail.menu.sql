INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970556979744770', '2015967849355186177', 'imgDetail', '图片详情表', 'menu', '/message/imgDetail', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970556979744771', '2015970556979744770', 'imgDetail_add', '新增', 'add', '/message/imgDetail/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970556979744772', '2015970556979744770', 'imgDetail_edit', '修改', 'edit', '/message/imgDetail/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970556979744773', '2015970556979744770', 'imgDetail_delete', '删除', 'delete', '/api/blade-message/imgDetail/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970556979744774', '2015970556979744770', 'imgDetail_view', '查看', 'view', '/message/imgDetail/view', 'file-text', 4, 2, 2, 1, NULL, 0);
