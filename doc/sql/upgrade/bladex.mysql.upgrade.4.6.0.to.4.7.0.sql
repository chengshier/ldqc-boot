-- -----------------------------------
-- 新增路由字段
-- -----------------------------------
ALTER TABLE `blade_top_menu`
    ADD COLUMN `path` varchar(255) NULL COMMENT '顶部菜单路由' AFTER `source`;
