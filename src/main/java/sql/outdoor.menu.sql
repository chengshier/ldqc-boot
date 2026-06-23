INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262311197384705', '2015967849355186177', 'outdoor', '户外活动表', 'menu', '/outdoor/outdoor', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262311197384706', '2031262311197384705', 'outdoor_add', '新增', 'add', '/outdoor/outdoor/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262311197384707', '2031262311197384705', 'outdoor_edit', '修改', 'edit', '/outdoor/outdoor/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262311197384708', '2031262311197384705', 'outdoor_delete', '删除', 'delete', '/api/blade-outdoor/outdoor/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262311197384709', '2031262311197384705', 'outdoor_view', '查看', 'view', '/outdoor/outdoor/view', 'file-text', 4, 2, 2, 1, NULL, 0);
