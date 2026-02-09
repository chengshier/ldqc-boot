INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970558405808129', '2015967849355186177', 'messageUserRelation', '消息用户关系表', 'menu', '/messageuserrelation/messageUserRelation', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970558405808130', '2015970558405808129', 'messageUserRelation_add', '新增', 'add', '/messageuserrelation/messageUserRelation/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970558405808131', '2015970558405808129', 'messageUserRelation_edit', '修改', 'edit', '/messageuserrelation/messageUserRelation/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970558405808132', '2015970558405808129', 'messageUserRelation_delete', '删除', 'delete', '/api/blade-messageuserrelation/messageUserRelation/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015970558405808133', '2015970558405808129', 'messageUserRelation_view', '查看', 'view', '/messageuserrelation/messageUserRelation/view', 'file-text', 4, 2, 2, 1, NULL, 0);
