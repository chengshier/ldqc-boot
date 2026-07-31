# 2026-07-30 运行时报错修复执行单

适用分支：`refactor/market-ready-phase1`  
适用数据库：MySQL 5.7

## 一、本次发现的问题

1. 商城查询 `mall_product` 时缺少 `sold_qty`，说明商城迁移未完整执行，且原迁移脚本此前漏写该字段。
2. 优惠券历史库将 `UNUSED/USED` 等业务状态写入了继承字段 `status`，Java 按整数读取时产生转换异常。
3. 部分历史优惠券模板使用 `FIXED`，但没有配置 `valid_end_at`，领取时提示有效期配置错误。
4. 培训详情 Vue 模板使用可选链，微信 WXML 编译器不支持。
5. 部分二级页同时使用原生导航和页面自绘导航，出现双导航栏。

## 二、数据库立即执行顺序

执行前备份：

```text
mall_product
mall_exchange_order
coupon_template
user_coupon
coupon_receive_log
```

### 商城

```text
1. doc/sql/mall-exchange-fulfillment-migration.sql
2. doc/sql/mall_product_runtime_compatibility_fix.sql
3. doc/sql/mall-exchange-fulfillment-data-fix.sql
```

说明：

- 第 1 份已修正，现会补 `sold_qty`；
- 第 2 份会按当前 `MallProductEntity` 补齐所有运行时字段，适合迁移执行不完整的历史库；
- 第 3 份规范商品类型、履约类型、限兑和历史订单履约状态。

### 优惠券

```text
4. doc/sql/coupon_receive_rule_integrity_upgrade.sql
5. doc/sql/coupon_runtime_compatibility_fix.sql
```

说明：

- 第 4 份补领取窗口、认证要求、限领和领取幂等；
- 第 5 份把历史 `status='UNUSED/USED/...'` 迁移到 `coupon_status`，恢复 `status` 的数值启停语义；
- `FIXED` 且没有结束时间的历史模板会迁移为 `RELATIVE`，默认领取后 30 天有效；
- 已正确配置固定结束时间的模板不会被改成相对有效期。

## 三、执行后验证 SQL

### 商城字段

```sql
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'mall_product'
  AND COLUMN_NAME IN (
    'sold_qty','gallery_json','category_code','category_name','spec_json',
    'exchange_notice','fulfillment_type','merchant_id','merchant_name',
    'pickup_address','per_user_limit','max_qty_per_order','require_address','published_at'
  )
ORDER BY COLUMN_NAME;
```

必须包含 `sold_qty`。

```sql
SELECT id, product_name, stock_total, stock_available, sold_qty,
       fulfillment_type, status
FROM mall_product
WHERE is_deleted = 0
LIMIT 20;
```

### 优惠券状态

```sql
SHOW COLUMNS FROM user_coupon LIKE 'status';
SHOW COLUMNS FROM user_coupon LIKE 'coupon_status';
```

期望：

- `status` 是 `tinyint`；
- `coupon_status` 是 `varchar`。

```sql
SELECT status, coupon_status, COUNT(*) AS row_count
FROM user_coupon
GROUP BY status, coupon_status
ORDER BY status, coupon_status;
```

`status` 中不得再出现 `UNUSED`、`USED` 等字符串。

### 优惠券有效期

```sql
SELECT id, coupon_name, valid_type, valid_start_at, valid_end_at, valid_days,
       receive_start_at, receive_end_at, status
FROM coupon_template
WHERE is_deleted = 0
ORDER BY id DESC;
```

要求：

- `FIXED` 必须有未来的 `valid_end_at`；
- `RELATIVE` 必须有大于 0 的 `valid_days`；
- 可领取模板 `status=1` 且库存大于 0。

## 四、代码修复

- `MallProductEntity` 对应迁移已补齐 `sold_qty`。
- 优惠券领取会在扣库存和扣绿豆前验证有效期，避免配置错误后才回滚。
- 新领取的用户券明确写入 `status=1`、`coupon_status='UNUSED'`。
- 小程序优惠券归一化不再使用通用数值 `status` 作为券业务状态。
- 培训详情模板中的可选链已移出 WXML 模板。
- 优惠券列表、关注/粉丝和消息页已统一为单一自定义导航配置。

## 五、重启与验证

执行 SQL 后：

1. 重启后端服务，避免旧 MyBatis 元数据或连接池状态干扰；
2. 清理微信开发者工具缓存并重新编译；
3. 依次验证商城列表、商品详情、优惠券领取、优惠券列表、培训详情；
4. 从个人中心进入关注、粉丝、优惠券、消息、赛事订单和场馆工作台，检查是否只显示一层导航栏。

当前代码已通过后端 Maven 编译；小程序仍需微信开发者工具重新编译确认 WXML 和真机表现。
