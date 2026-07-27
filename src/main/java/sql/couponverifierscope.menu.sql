-- 优惠券核销员授权管理菜单。执行后请在角色管理中授予运营管理员。
INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted) VALUES
('205150012','0','couponVerifierScope','核销员授权','menu','/couponverifierscope/couponVerifierScope',NULL,22,1,0,1,'优惠券核销员与适用范围授权',0),
('205150013','205150012','couponVerifierScope_add','新增','add','/couponverifierscope/couponVerifierScope/add','plus',1,2,1,1,NULL,0),
('205150014','205150012','couponVerifierScope_edit','修改','edit','/couponverifierscope/couponVerifierScope/edit','form',2,2,2,1,NULL,0),
('205150015','205150012','couponVerifierScope_delete','删除','delete','/api/blade-couponverifierscope/couponVerifierScope/remove','delete',3,2,3,1,NULL,0),
('205150016','205150012','couponVerifierScope_view','查看','view','/couponverifierscope/couponVerifierScope/view','file-text',4,2,2,1,NULL,0);
