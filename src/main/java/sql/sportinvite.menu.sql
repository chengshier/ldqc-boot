INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354343007985666', '2015967849355186177', 'sportInvite', '运动邀约表', 'menu', '/sportinvite/sportInvite', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354343007985667', '2057354343007985666', 'sportInvite_add', '新增', 'add', '/sportinvite/sportInvite/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354343007985668', '2057354343007985666', 'sportInvite_edit', '修改', 'edit', '/sportinvite/sportInvite/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354343007985669', '2057354343007985666', 'sportInvite_delete', '删除', 'delete', '/api/blade-sportinvite/sportInvite/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2057354343007985670', '2057354343007985666', 'sportInvite_view', '查看', 'view', '/sportinvite/sportInvite/view', 'file-text', 4, 2, 2, 1, NULL, 0);
