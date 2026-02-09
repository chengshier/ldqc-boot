-- -----------------------------------
-- 新增路由字段
-- -----------------------------------
ALTER TABLE "BLADE_TOP_MENU" ADD "PATH" VARCHAR2(255);

COMMENT ON COLUMN "BLADE_TOP_MENU"."PATH" IS '顶部菜单路由';
