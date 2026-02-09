INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015969184553144321', '2015967849355186177', 'agreeCollect', '点赞收藏表', 'menu', '/agreecollect/agreeCollect', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015969184553144322', '2015969184553144321', 'agreeCollect_add', '新增', 'add', '/agreecollect/agreeCollect/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015969184553144323', '2015969184553144321', 'agreeCollect_edit', '修改', 'edit', '/agreecollect/agreeCollect/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015969184553144324', '2015969184553144321', 'agreeCollect_delete', '删除', 'delete', '/api/blade-agreecollect/agreeCollect/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2015969184553144325', '2015969184553144321', 'agreeCollect_view', '查看', 'view', '/agreecollect/agreeCollect/view', 'file-text', 4, 2, 2, 1, NULL, 0);
