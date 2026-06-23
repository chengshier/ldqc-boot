INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031616456760688641', '2015967849355186177', 'talentPost', '达人动态表', 'menu', '/talentpost/talentPost', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031616456760688642', '2031616456760688641', 'talentPost_add', '新增', 'add', '/talentpost/talentPost/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031616456760688643', '2031616456760688641', 'talentPost_edit', '修改', 'edit', '/talentpost/talentPost/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031616456760688644', '2031616456760688641', 'talentPost_delete', '删除', 'delete', '/api/blade-talentpost/talentPost/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2031616456760688645', '2031616456760688641', 'talentPost_view', '查看', 'view', '/talentpost/talentPost/view', 'file-text', 4, 2, 2, 1, NULL, 0);
