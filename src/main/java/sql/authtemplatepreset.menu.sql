INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2042158704891928578', '2041796488564969474', 'authTemplatePreset', '认证模板推荐项(字段/附件)', 'menu', '/authtemplatepreset/authTemplatePreset', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2042158704891928579', '2042158704891928578', 'authTemplatePreset_add', '新增', 'add', '/authtemplatepreset/authTemplatePreset/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2042158704891928580', '2042158704891928578', 'authTemplatePreset_edit', '修改', 'edit', '/authtemplatepreset/authTemplatePreset/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2042158704891928581', '2042158704891928578', 'authTemplatePreset_delete', '删除', 'delete', '/api/blade-authtemplatepreset/authTemplatePreset/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO blade_menu(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES ('2042158704891928582', '2042158704891928578', 'authTemplatePreset_view', '查看', 'view', '/authtemplatepreset/authTemplatePreset/view', 'file-text', 4, 2, 2, 1, NULL, 0);
