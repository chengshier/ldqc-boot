INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350698582069249', '2028307934607712257', 'newsUserAction', '用户行为表', 'menu', '/newsuseraction/newsUserAction', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350698582069250', '2028350698582069249', 'newsUserAction_add', '新增', 'add', '/newsuseraction/newsUserAction/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350698582069251', '2028350698582069249', 'newsUserAction_edit', '修改', 'edit', '/newsuseraction/newsUserAction/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350698582069252', '2028350698582069249', 'newsUserAction_delete', '删除', 'delete', '/api/blade-newsuseraction/newsUserAction/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2028350698582069253', '2028350698582069249', 'newsUserAction_view', '查看', 'view', '/newsuseraction/newsUserAction/view', 'file-text', 4, 2, 2, 1, NULL, 0);
