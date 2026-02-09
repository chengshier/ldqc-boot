-- -----------------------------------
-- 新增路由字段
-- -----------------------------------
ALTER TABLE "blade_top_menu"
    ADD COLUMN "path" varchar(255);

COMMENT ON COLUMN "blade_top_menu"."path" IS '顶部菜单路由';
