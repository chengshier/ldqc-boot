# 第一批：P0、数据库与运行阻断修复记录

> 分支：`refactor/market-ready-phase1`  
> 日期：2026-07-30  
> 范围：推荐反馈 404、场馆无效参数、BladeX 基础字段、优惠券核销日志、商城订单状态、管理端动态路由。

## 一、代码修复

### 推荐反馈

- 新增 `RecommendFeedbackRequest`，明确 `requestId、sessionId、contentType、contentId、eventType、durationMs、extraJson`。
- 新增 `POST /blade-recommend/feedback`。
- 用户 ID 仅取服务端登录态。
- `requestId` 重复按幂等成功处理。
- GitHub Actions 增加接口和服务端身份边界检查。

### 场馆请求

- 小程序请求层统一删除 GET 参数中的 `undefined、null、空字符串、"undefined"、"null"`。
- 保留 `0` 和 `false`。
- 场馆分页仅使用 BladeX 正式分页字段 `current、size`，业务筛选仅使用 `keyword、typeId`。
- “全部”分类不发送 `typeId`。
- 增加请求版本号，快速切换分类时旧请求不能覆盖新结果。

### BladeX 表字段

- `ldqc_training_access`、`ldqc_training_progress` 原迁移补齐通用 `status`。
- `coupon_verifier_scope` 原迁移补齐 `create_dept` 并统一基础字段定义。
- `coupon_verify_log` 增加独立运行时修复脚本。
- 新增全量基础字段兼容脚本和只读结构验证矩阵。

### 商城订单状态

- `MallExchangeOrderEntity.orderStatus` 不再映射到通用 `status`。
- 独立使用 `order_status` 保存 `CREATED/SUCCESS/FAILED/CANCELLED/COMPLETED`。
- 通用 `status` 恢复数值启停语义。
- 新增历史数据迁移和业务状态索引。

### 管理端空白页面

- 已定位 BladeX 动态路由按 `views + menu.path` 查找组件。
- 原课程和商城菜单使用连字符路径，但实际 Vue 文件使用驼峰文件名，导致组件无法解析。
- 已修正：
  - `/training/courseManager`
  - `/training/courseWorkbench`
  - `/mall/productManager`
  - `/mall/fulfillmentWorkbench`
- 增加旧数据库菜单路径修复脚本。
- `mobile-web-app-capable` meta 已补充；该警告不是空白页根因。

## 二、当前数据库推荐执行顺序

以下顺序适用于已经执行过部分旧脚本的测试库。执行前先备份相关表。

```text
01 doc/sql/recommend_feedback_upgrade.sql
02 doc/sql/training-video-course-migration.sql
03 doc/sql/coupon-security-migration.sql
04 doc/sql/coupon_receive_rule_integrity_upgrade.sql
05 doc/sql/coupon_runtime_compatibility_fix.sql
06 doc/sql/mall-exchange-fulfillment-migration.sql
07 doc/sql/mall_product_runtime_compatibility_fix.sql
08 doc/sql/mall-exchange-fulfillment-data-fix.sql
09 doc/sql/mall_exchange_order_status_runtime_fix.sql
10 doc/sql/blade_base_fields_compatibility_upgrade.sql
11 doc/sql/coupon_verify_log_runtime_fix.sql
12 doc/sql/market_ready_phase1_data_cleanup.sql
13 src/main/java/sql/trainingcourseworkbench.menu.sql
14 src/main/java/sql/malloperation.menu.sql
15 src/main/java/sql/contentmoderation.menu.sql
16 doc/sql/venue_onboarding_menu_seed.sql
17 doc/sql/market_ready_admin_route_fix.sql
18 doc/sql/market_ready_phase1_schema_validation.sql
```

说明：

- 第 1～8 步可重复执行，用于确保历史环境没有遗漏原业务迁移；
- 第 9～12 步是本批新增的运行时兼容与数据修复；
- 第 13～17 步修正菜单及现有数据库动态路由；
- 第 18 步只读，必须保存结果作为验收材料；
- 运行库缺少基础业务表时，先执行项目基础建表 SQL，不允许由兼容脚本猜测创建完整业务表。

## 三、必须备份的表

```text
ldqc_recommend_feedback
ldqc_training
ldqc_training_chapter
ldqc_training_lesson
ldqc_training_access
ldqc_training_progress
coupon_template
coupon_receive_log
coupon_verifier_scope
coupon_verify_log
user_coupon
mall_product
mall_exchange_order
blade_menu
```

## 四、执行后操作

1. 重启后端，避免连接池、MyBatis 元数据或旧部署包影响验证。
2. 管理端退出登录并重新登录，必要时清理菜单缓存与浏览器缓存。
3. 小程序清理微信开发者工具缓存并重新编译。
4. 验证推荐反馈不再 404。
5. 验证场馆“全部”和分类切换请求不包含 `undefined`。
6. 验证优惠券详情、核销记录和核销确认。
7. 验证课程管理、课程内容工作台、商品管理、兑换履约页面可正常打开。
8. 保存 `market_ready_phase1_schema_validation.sql` 的全部结果。

## 五、仍需真实环境验证

- MySQL 5.7 首次执行、第二次重复执行和回滚演练；
- 后端 Maven 编译与接口调用；
- 管理端生产构建、菜单权限和页面请求；
- HBuilderX、微信开发者工具和真机；
- 多租户、普通用户、达人、场馆运营者、核销员与运营管理员。

本文件不把尚未执行的数据库和运行环境验证标记为通过。
