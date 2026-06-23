INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541330191798274', '2015967849355186177', 'trainingTeacher', '培训教练表', 'menu', '/trainingteacher/trainingTeacher', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541330191798275', '2039541330191798274', 'trainingTeacher_add', '新增', 'add', '/trainingteacher/trainingTeacher/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541330191798276', '2039541330191798274', 'trainingTeacher_edit', '修改', 'edit', '/trainingteacher/trainingTeacher/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541330191798277', '2039541330191798274', 'trainingTeacher_delete', '删除', 'delete', '/api/blade-trainingteacher/trainingTeacher/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2039541330191798278', '2039541330191798274', 'trainingTeacher_view', '查看', 'view', '/trainingteacher/trainingTeacher/view', 'file-text', 4, 2, 2, 1, NULL, 0);
