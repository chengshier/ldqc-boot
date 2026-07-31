# market-ready-phase1 BladeX 基础字段与业务状态矩阵

更新日期：2026-07-30

## 一、统一基础字段

凡 Java Entity 继承 `TenantEntity` 的本轮业务表，数据库必须提供：

| 字段 | 用途 | 约束 |
|---|---|---|
| `id` | 主键 | `bigint`，非空 |
| `tenant_id` | 租户 | `varchar(12)`，默认 `000000` |
| `create_user` | 创建人 | `bigint` |
| `create_dept` | 创建部门 | `bigint` |
| `create_time` | 创建时间 | `datetime` |
| `update_user` | 修改人 | `bigint` |
| `update_time` | 修改时间 | `datetime` |
| `status` | 数据启停状态 | 数值类型，默认 `1` |
| `is_deleted` | 逻辑删除 | 数值类型，默认 `0` |

`status` 只允许表示数据启停，不允许保存订单、审核、播放、核销、预约等业务枚举。

## 二、本轮重点表

| 表 | Entity 基类 | 独立业务状态字段 | 基础字段补丁 |
|---|---|---|---|
| `ldqc_recommend_feedback` | `TenantEntity` | `event_type` | `blade_base_fields_compatibility_upgrade.sql` |
| `ldqc_training` | `TenantEntity` | `publish_status` | 课程迁移 + 基础字段补丁 |
| `ldqc_training_chapter` | `TenantEntity` | 无 | 课程迁移 + 基础字段补丁 |
| `ldqc_training_lesson` | `TenantEntity` | `media_process_status` | 课程迁移 + 基础字段补丁 |
| `ldqc_training_access` | `TenantEntity` | `access_status` | 课程迁移 + 基础字段补丁 |
| `ldqc_training_progress` | `TenantEntity` | `completed` | 课程迁移 + 基础字段补丁 |
| `ldqc_training_booking` | `TenantEntity` | `booking_status` | `training_booking_migration.sql` + 基础字段补丁 |
| `competition_signup` | `TenantEntity` | `signup_status/payment_status` | 赛事迁移 + 基础字段补丁 |
| `content_audit_task` | `TenantEntity` | `audit_status/process_status` | 内容审核迁移 + 基础字段补丁 |
| `coupon_template` | `TenantEntity` | 模板规则字段 | 优惠券迁移 + 基础字段补丁 |
| `coupon_receive_log` | `TenantEntity` | 领取结果字段 | 优惠券迁移 + 基础字段补丁 |
| `coupon_verifier_scope` | `TenantEntity` | 范围类型字段 | `coupon-security-migration.sql` + 基础字段补丁 |
| `coupon_verify_log` | `TenantEntity` | `verify_status` | `coupon_verify_log_runtime_fix.sql` |
| `user_coupon` | `TenantEntity` | `coupon_status` | `coupon_runtime_compatibility_fix.sql` |
| `mall_product` | `TenantEntity` | 商品业务字段 | 商城迁移 + 基础字段补丁 |
| `mall_exchange_order` | `TenantEntity` | `order_status` | `mall_exchange_order_status_runtime_fix.sql` |
| `venue_apply` | `TenantEntity` | 入驻审核状态字段 | 场馆迁移 + 基础字段补丁 |

## 三、已确认的状态字段分离

| 错误/风险 | 修复后 |
|---|---|
| `user_coupon.status='UNUSED/USED'` | `status=1/0`，业务状态存 `coupon_status` |
| `mall_exchange_order.status='CREATED/PAID/...'` | `status=1/0`，业务状态存 `order_status` |
| `coupon_verify_log` 实体读取不存在的 `verify_status` | 补 `verify_status` 并回填历史值 |
| 课程发布状态写进通用 `status` | 发布状态存 `publish_status` |
| 视频处理状态写进通用 `status` | 媒体状态存 `media_process_status` |
| 课程预约状态写进通用 `status` | 预约状态存 `booking_status` |

## 四、执行和验证文件

```text
doc/sql/blade_base_fields_compatibility_upgrade.sql
doc/sql/coupon_verify_log_runtime_fix.sql
doc/sql/coupon_runtime_compatibility_fix.sql
doc/sql/mall_exchange_order_status_runtime_fix.sql
doc/sql/training_booking_migration.sql
doc/sql/market_ready_phase1_schema_validation.sql
```

验证结果要求：

- 已安装表的九项基础字段均存在；
- `status` 均为数值类型；
- 业务枚举误写进通用 `status` 的记录数为 0；
- Java Entity 字段、数据库列名和接口 DTO 一一对应。
