INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023445026205697', '2015967849355186177', 'bannerPosition', '宣传Banner位置表', 'menu', '/bannerposition/bannerPosition', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023445026205698', '2074023445026205697', 'bannerPosition_add', '新增', 'add', '/bannerposition/bannerPosition/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023445026205699', '2074023445026205697', 'bannerPosition_edit', '修改', 'edit', '/bannerposition/bannerPosition/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023445026205700', '2074023445026205697', 'bannerPosition_delete', '删除', 'delete', '/api/blade-bannerposition/bannerPosition/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023445026205701', '2074023445026205697', 'bannerPosition_view', '查看', 'view', '/bannerposition/bannerPosition/view', 'file-text', 4, 2, 2, 1, NULL, 0);
