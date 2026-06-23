INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262312799608834', '2015967849355186177', 'training', '培训课程表', 'menu', '/training/training', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262312799608835', '2031262312799608834', 'training_add', '新增', 'add', '/training/training/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262312799608836', '2031262312799608834', 'training_edit', '修改', 'edit', '/training/training/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262312799608837', '2031262312799608834', 'training_delete', '删除', 'delete', '/api/blade-training/training/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031262312799608838', '2031262312799608834', 'training_view', '查看', 'view', '/training/training/view', 'file-text', 4, 2, 2, 1, NULL, 0);
