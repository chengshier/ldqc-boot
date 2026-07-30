# market-ready-phase1 新增表与 BladeX 字段一致性矩阵

> 分支：`refactor/market-ready-phase1`  
> 静态核对日期：2026-07-30  
> 本矩阵记录代码和 SQL 的静态结论；数据库真实字段仍必须执行 `doc/sql/market_ready_phase1_schema_validation.sql` 后确认。

## 统一基线

继承 `TenantEntity` 的表至少应具备：

`id、tenant_id、create_user、create_dept、create_time、update_user、update_time、status、is_deleted`

其中：

- `status` 只表示 BladeX 通用启停状态，必须为数值类型；
- 审核、订单、券、授权、发布等业务状态必须使用独立字段；
- 已存在字段不通过前端或 Java 多字段兼容掩盖，数据库、Entity 和 DTO 必须统一。

## 静态矩阵

| 表名 | Entity | 继承基类 | 静态发现 | 本批处理 | 当前结论 |
|---|---|---|---|---|---|
| `content_audit_task` | `ContentAuditTaskEntity` | `TenantEntity` | 创建脚本包含完整基础字段 | 保留并纳入验证脚本 | 静态完整，待数据库验证 |
| `ldqc_recommend_feedback` | `RecommendFeedbackEntity` | `TenantEntity` | 创建脚本包含完整基础字段和幂等索引 | 新增正式反馈 Controller/DTO；纳入验证 | 静态完整，待数据库验证 |
| `ldqc_training_chapter` | `TrainingChapterEntity` | `TenantEntity` | 基础字段完整 | 纳入基础字段补丁和验证 | 静态完整，待数据库验证 |
| `ldqc_training_lesson` | `TrainingLessonEntity` | `TenantEntity` | 基础字段完整 | 纳入基础字段补丁和验证 | 静态完整，待数据库验证 |
| `ldqc_training_access` | `TrainingAccessEntity` | `TenantEntity` | 原创建脚本缺少通用 `status` | 已修正原迁移；兼容补丁可为旧库补列 | 已修复，待数据库验证 |
| `ldqc_training_progress` | `TrainingProgressEntity` | `TenantEntity` | 原创建脚本缺少通用 `status` | 已修正原迁移；兼容补丁可为旧库补列 | 已修复，待数据库验证 |
| `ldqc_venue_apply` | `VenueApplyEntity` | `TenantEntity` | 创建脚本基础字段完整，业务状态使用 `apply_status` | 纳入验证 | 静态完整，待数据库验证 |
| `coupon_verifier_scope` | `CouponVerifierScopeEntity` | `TenantEntity` | 原创建脚本缺少 `create_dept`，部分字段类型与统一基线不一致 | 已修正原迁移；兼容补丁补旧库字段 | 已修复，待数据库验证 |
| `coupon_verify_log` | `CouponVerifyLogEntity` | `TenantEntity` | 运行库已确认缺少 `verify_status`；历史表基础字段也可能不完整 | 新增 `coupon_verify_log_runtime_fix.sql` | 已提供修复，待执行 |
| `user_coupon` | `UserCouponEntity` | `TenantEntity` | 历史 `status` 曾存券业务状态 | `coupon_runtime_compatibility_fix.sql` 和数据清理恢复数值 `status`，业务状态写入 `coupon_status` | 已提供修复，待执行 |
| `mall_exchange_order` | `MallExchangeOrderEntity` | `TenantEntity` | 原 Entity 将字符串订单状态映射到通用 `status`，并同时调用 `setStatus(1)`，存在字段与类型冲突 | Entity 改用独立 `order_status`；新增运行时迁移和索引 | 已修复，待执行与编译 |
| `ldqc_competition_signup` | `CompetitionSignupEntity` | `TenantEntity` | 业务状态使用独立 `order_status` | 纳入基础字段和结构验证 | 静态合理，待数据库验证 |
| `coupon_receive_log` | `CouponReceiveLogEntity` | `TenantEntity` | 本轮增加幂等和领取规则字段，实际基础字段依赖历史表 | 纳入基础字段补丁和验证 | 待数据库验证 |
| `mall_product` | `MallProductEntity` | `TenantEntity` | 本轮增加运营字段；历史库曾出现字段迁移不完整 | 纳入基础字段补丁、运行时兼容和验证 | 待数据库验证 |
| `ldqc_training` | `TrainingEntity` | `TenantEntity` | 业务发布状态使用独立 `publish_status` | 纳入基础字段、数据清理和验证 | 静态合理，待数据库验证 |
| `ldqc_venue` | `VenueEntity` | `TenantEntity` | 本轮新增运营者和来源申请字段 | 纳入基础字段和验证 | 待数据库验证 |
| `ldqc_talent_post` | `TalentPostEntity` | `TenantEntity` | 新增来源社区内容字段和唯一索引 | 纳入基础字段和验证 | 待数据库验证 |
| `t_img_detail` | `ImgDetailEntity` | `TenantEntity` | 新增审核、发布和媒体处理字段 | 纳入基础字段和验证 | 待数据库验证 |
| `t_follow` | `FollowEntity` | `TenantEntity` | 新增有效唯一键并清理重复关系 | 纳入基础字段和验证 | 待数据库验证 |

## 本批新增防回归措施

1. `doc/sql/blade_base_fields_compatibility_upgrade.sql`：为旧库补充缺失的非主键基础字段。
2. `doc/sql/market_ready_phase1_schema_validation.sql`：输出表、基础字段、业务字段和关键索引矩阵。
3. 后端 GitHub Actions 增加基础字段与业务状态静态门禁。
4. 商城订单禁止恢复 `@TableField("status")` 业务状态映射。
5. 新建表原始迁移脚本同步修正，避免新环境继续生成错误结构。

## 验收结论规则

- **完整**：数据库真实字段、类型、默认值、Entity 和索引全部一致；
- **缺字段**：Entity 继承字段在表中不存在；
- **类型不一致**：Java `Integer/Long/String` 与数据库字段类型不匹配；
- **业务状态误用**：字符串业务枚举写入通用 `status`；
- **仅 SQL 无 Entity / 有 Entity 无表**：必须补齐或删除无效规划；
- **待数据库验证**：仅完成代码静态核对，不能标记为环境验收通过。
