INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633840075407361', '2015967849355186177', 'userAuthApply', '用户认证申请表', 'menu', '/userauthapply/userAuthApply', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633840075407362', '2039633840075407361', 'userAuthApply_add', '新增', 'add', '/userauthapply/userAuthApply/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633840075407363', '2039633840075407361', 'userAuthApply_edit', '修改', 'edit', '/userauthapply/userAuthApply/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633840075407364', '2039633840075407361', 'userAuthApply_delete', '删除', 'delete', '/api/blade-userauthapply/userAuthApply/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039633840075407365', '2039633840075407361', 'userAuthApply_view', '查看', 'view', '/userauthapply/userAuthApply/view', 'file-text', 4, 2, 2, 1, NULL, 0);
