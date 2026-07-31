-- 积分商城运营菜单
-- 页面：
--   src/views/mall/productManager.vue
--   src/views/mall/fulfillmentWorkbench.vue
-- BladeX 动态路由会按 path 拼接为 views + path，大小写和文件名必须完全一致。
-- 优惠券继续使用独立菜单，不挂在商城运营下。

INSERT INTO blade_menu
(id, parent_id, code, name, alias, path, source, sort, category, action, is_open, remark, is_deleted)
VALUES
('205180000', '0', 'mallOperation', '商城运营', 'menu', '/mall', 'iconfont icon-shangcheng', 18, 1, 0, 1, '积分商城商品和兑换订单运营入口', 0),
('205180001', '205180000', 'mallProductManager', '商品管理', 'menu', '/mall/productManager', 'iconfont icon-shangpin', 1, 1, 0, 1, '维护商品图片、规格、库存、限兑和履约方式', 0),
('205180002', '205180000', 'mallFulfillmentWorkbench', '兑换履约', 'menu', '/mall/fulfillmentWorkbench', 'iconfont icon-dingdan', 2, 1, 0, 1, '发货、到店领取、虚拟权益和取消退款工作台', 0),
('205180011', '205180001', 'mallProduct_edit', '编辑商品', 'edit', '/api/blade-mall/product-admin/save', 'edit', 1, 2, 2, 1, '新增或修改商城商品', 0),
('205180012', '205180001', 'mallProduct_status', '商品上下架', 'status', '/api/blade-mall/product-admin/status', 'check', 2, 2, 2, 1, '校验商品完整性后上架或下架', 0),
('205180021', '205180002', 'mallOrder_ship', '订单发货', 'ship', '/api/blade-mall/exchange/admin-ship', 'transport', 1, 2, 2, 1, '填写物流公司和物流单号', 0),
('205180022', '205180002', 'mallOrder_pickup', '到店领取', 'pickup', '/api/blade-mall/exchange/admin-ready-pickup', 'location', 2, 2, 2, 1, '设置待领取并核验领取码', 0),
('205180023', '205180002', 'mallOrder_virtual', '虚拟权益发放', 'virtual', '/api/blade-mall/exchange/admin-issue-virtual', 'link', 3, 2, 2, 1, '发放兑换码或权益说明', 0),
('205180024', '205180002', 'mallOrder_cancel', '取消退款', 'cancel', '/api/blade-mall/exchange/admin-cancel', 'refund', 4, 2, 2, 1, '恢复库存、退还绿豆并记录流水', 0)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
path = VALUES(path),
source = VALUES(source),
sort = VALUES(sort),
remark = VALUES(remark),
is_deleted = 0;
