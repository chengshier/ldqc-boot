INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541335334014978', '2015967849355186177', 'venueFacility', '场馆设施表', 'menu', '/venuefacility/venueFacility', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541335334014979', '2039541335334014978', 'venueFacility_add', '新增', 'add', '/venuefacility/venueFacility/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541335334014980', '2039541335334014978', 'venueFacility_edit', '修改', 'edit', '/venuefacility/venueFacility/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541335334014981', '2039541335334014978', 'venueFacility_delete', '删除', 'delete', '/api/blade-venuefacility/venueFacility/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541335334014982', '2039541335334014978', 'venueFacility_view', '查看', 'view', '/venuefacility/venueFacility/view', 'file-text', 4, 2, 2, 1, NULL, 0);
