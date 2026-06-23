INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541328384053249', '2015967849355186177', 'trainingOrg', '培训机构表', 'menu', '/trainingorg/trainingOrg', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541328384053250', '2039541328384053249', 'trainingOrg_add', '新增', 'add', '/trainingorg/trainingOrg/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541328384053251', '2039541328384053249', 'trainingOrg_edit', '修改', 'edit', '/trainingorg/trainingOrg/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541328384053252', '2039541328384053249', 'trainingOrg_delete', '删除', 'delete', '/api/blade-trainingorg/trainingOrg/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541328384053253', '2039541328384053249', 'trainingOrg_view', '查看', 'view', '/trainingorg/trainingOrg/view', 'file-text', 4, 2, 2, 1, NULL, 0);
