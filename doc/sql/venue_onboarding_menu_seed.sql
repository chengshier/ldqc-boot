-- 场馆入驻审核菜单与按钮权限
-- 执行后请在角色管理中将菜单授予场馆运营或平台管理员。

INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted)
SELECT '205150050','0','venueApplyAudit','场馆入驻审核','menu','/venueapply/venueApply',NULL,25,1,0,1,'场馆入驻申请审核工作台',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='venueApplyAudit' AND is_deleted=0);

INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted)
SELECT '205150051','205150050','venueApplyAudit_view','查看申请','view','/venueapply/venueApply/view','file-text',1,2,2,1,'查看场馆入驻资料',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='venueApplyAudit_view' AND is_deleted=0);

INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted)
SELECT '205150052','205150050','venueApplyAudit_approve','审核通过','approve','/api/blade-venue/venue-apply/admin/audit','circle-check',2,2,3,1,'审核通过并创建场馆',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='venueApplyAudit_approve' AND is_deleted=0);

INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted)
SELECT '205150053','205150050','venueApplyAudit_reject','驳回申请','reject','/api/blade-venue/venue-apply/admin/audit','circle-close',3,2,3,1,'驳回场馆入驻申请',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='venueApplyAudit_reject' AND is_deleted=0);
