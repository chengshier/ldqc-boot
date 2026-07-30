-- 优惠券兑换与核销安全加固
-- 执行后请为每个允许核销的账号配置一条精确授权记录。
-- CouponVerifierScopeEntity 继承 TenantEntity，表结构必须包含完整 BladeX 基础字段。

CREATE TABLE IF NOT EXISTS `coupon_verifier_scope` (
  `id` bigint NOT NULL COMMENT '主键',
  `verifier_user_id` bigint NOT NULL COMMENT '核销员用户ID',
  `scope_type` varchar(32) NOT NULL COMMENT 'ALL/VENUE/CAMP/COURSE/GOODS，与coupon_template.scope_type一致',
  `scope_ref_id` varchar(128) NOT NULL COMMENT '适用范围ID；全场核销使用ALL',
  `venue_name` varchar(128) DEFAULT NULL COMMENT '核销端展示名称',
  `status` int NOT NULL DEFAULT 1 COMMENT '数据状态 1正常0停用',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常1删除',
  `tenant_id` varchar(12) NOT NULL DEFAULT '000000' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_verifier_scope` (`verifier_user_id`, `scope_type`, `scope_ref_id`, `is_deleted`),
  KEY `idx_verifier_enabled` (`verifier_user_id`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券核销员授权范围';

-- 示例：将用户 10001 授权为全场核销员。实际ID由项目的雪花ID生成策略提供。
-- INSERT INTO coupon_verifier_scope
-- (id, verifier_user_id, scope_type, scope_ref_id, venue_name, status, create_time, update_time, is_deleted, tenant_id)
-- VALUES (10000001, 10001, 'ALL', 'ALL', '示例场馆', 1, NOW(), NOW(), 0, '000000');
