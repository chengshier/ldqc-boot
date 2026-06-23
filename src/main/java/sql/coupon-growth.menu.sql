-- 可选：菜单初始化（请按你们实际上级菜单ID调整 parent_id）
-- growthlevelconfig
INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted)
VALUES ('205140001','0','growthLevelConfig','成长等级配置','menu','/growthlevelconfig/growthLevelConfig',NULL,1,1,0,1,NULL,0);
-- coupontemplate
INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted)
VALUES ('205140002','0','couponTemplate','优惠券模板','menu','/coupontemplate/couponTemplate',NULL,2,1,0,1,NULL,0);
-- couponverifylog
INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted)
VALUES ('205140003','0','couponVerifyLog','优惠券核销日志','menu','/couponverifylog/couponVerifyLog',NULL,3,1,0,1,NULL,0);
