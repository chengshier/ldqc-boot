INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023443382038530', '2015967849355186177', 'bannerItem', '宣传Banner内容表', 'menu', '/banneritem/bannerItem', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023443382038531', '2074023443382038530', 'bannerItem_add', '新增', 'add', '/banneritem/bannerItem/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023443382038532', '2074023443382038530', 'bannerItem_edit', '修改', 'edit', '/banneritem/bannerItem/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023443382038533', '2074023443382038530', 'bannerItem_delete', '删除', 'delete', '/api/blade-banneritem/bannerItem/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2074023443382038534', '2074023443382038530', 'bannerItem_view', '查看', 'view', '/banneritem/bannerItem/view', 'file-text', 4, 2, 2, 1, NULL, 0);
