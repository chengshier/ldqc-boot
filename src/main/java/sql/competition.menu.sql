INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262309523857409', '2015967849355186177', 'competition', '赛事表', 'menu', '/competition/competition', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262309523857410', '2031262309523857409', 'competition_add', '新增', 'add', '/competition/competition/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262309523857411', '2031262309523857409', 'competition_edit', '修改', 'edit', '/competition/competition/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262309523857412', '2031262309523857409', 'competition_delete', '删除', 'delete', '/api/blade-competition/competition/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262309523857413', '2031262309523857409', 'competition_view', '查看', 'view', '/competition/competition/view', 'file-text', 4, 2, 2, 1, NULL, 0);
