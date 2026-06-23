-- 新增模块菜单（请按实际父级菜单ID调整 parent_id）
INSERT INTO blade_menu(id,parent_id,code,name,alias,path,source,sort,category,action,is_open,remark,is_deleted) VALUES
('205150001','0','pointsAccount','积分账户','menu','/pointsaccount/pointsAccount',NULL,11,1,0,1,NULL,0),
('205150002','0','pointsRule','积分规则','menu','/pointsrule/pointsRule',NULL,12,1,0,1,NULL,0),
('205150003','0','pointsDailyCounter','积分日统计','menu','/pointsdailycounter/pointsDailyCounter',NULL,13,1,0,1,NULL,0),
('205150004','0','pointsTaskLog','积分任务日志','menu','/pointstasklog/pointsTaskLog',NULL,14,1,0,1,NULL,0),
('205150005','0','pointsLedger','积分流水','menu','/pointsledger/pointsLedger',NULL,15,1,0,1,NULL,0),
('205150006','0','pointsSigninStat','签到统计','menu','/pointssigninstat/pointsSigninStat',NULL,16,1,0,1,NULL,0),
('205150007','0','couponReceiveLog','领券日志','menu','/couponreceivelog/couponReceiveLog',NULL,17,1,0,1,NULL,0),
('205150008','0','userCoupon','用户优惠券','menu','/usercoupon/userCoupon',NULL,18,1,0,1,NULL,0),
('205150009','0','mallProduct','商城商品','menu','/mallproduct/mallProduct',NULL,19,1,0,1,NULL,0),
('205150010','0','mallExchangeOrder','商城兑换订单','menu','/mallexchangeorder/mallExchangeOrder',NULL,20,1,0,1,NULL,0),
('205150011','0','ruleVersionNotice','规则公示','menu','/ruleversionnotice/ruleVersionNotice',NULL,21,1,0,1,NULL,0);
