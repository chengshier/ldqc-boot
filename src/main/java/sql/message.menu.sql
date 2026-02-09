INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970555432046593', '2015967849355186177', 'message', '消息表', 'menu', '/message/message', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970555432046594', '2015970555432046593', 'message_add', '新增', 'add', '/message/message/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970555432046595', '2015970555432046593', 'message_edit', '修改', 'edit', '/message/message/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970555432046596', '2015970555432046593', 'message_delete', '删除', 'delete', '/api/blade-message/message/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970555432046597', '2015970555432046593', 'message_view', '查看', 'view', '/message/message/view', 'file-text', 4, 2, 2, 1, NULL, 0);
