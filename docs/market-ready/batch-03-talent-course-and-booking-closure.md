# 第三批：达人课程生产、平台审核与线下预约闭环

更新日期：2026-07-30  
适用分支：`refactor/market-ready-phase1`

## 一、产品边界

本批完成两条真实链路：

1. 已认证达人维护本人课程、章节、课时和长视频，提交平台审核；
2. 普通用户预约已发布的线下或混合课程，平台确认、驳回或完成预约。

本批不伪造微信课程支付：

- 课程价格只作为快照展示；
- `purchaseRequired` 表示需要购买或授权，不表示已经付款；
- 没有微信预支付、支付回调、退款和对账基础设施时，页面明确显示“付费购买暂未开放”；
- 线上正式课时仍可由平台授权用户观看，试看课时可按配置播放。

## 二、达人课程生产链路

### 1. 身份和数据权限

- 达人身份只以后端登录用户为准。
- 只有 `isTalent=1` 且 `authStatus=2` 的用户可以进入工作台。
- 客户端不能提交或覆盖 `talentUserId`。
- 每次课程、章节、课时读写都校验记录属于当前达人。
- 达人不能直接发布课程，也不能给其他用户发放播放权限。

### 2. 达人可操作内容

- 新建和编辑课程资料；
- 上传课程封面；
- 创建、编辑、删除空章节；
- 创建、编辑和删除视频课时；
- 上传长视频和自定义封面；
- 配置免费试看；
- 视频处理失败后重新处理；
- 查看视频处理状态和课程完整度；
- 提交平台审核；
- 主动下架本人已发布课程；
- 删除没有章节课时的草稿、驳回或下架课程。

### 3. 课程审核状态

```text
DRAFT/REJECTED/OFFLINE
  → 达人完善资料和课时
  → 提交审核 PENDING
  → 平台通过 PUBLISHED
  → 平台驳回 REJECTED
```

线上或混合课程提交审核前必须：

- 有课程标题、封面和介绍；
- 至少有一个启用课时；
- 视频课时已上传；
- 视频处理状态为 `READY`。

## 三、管理端课程审核

新增运营页面：

- 课程审核列表；
- 查看课程资料、章节、课时、试看和视频处理状态；
- 通过并发布；
- 填写原因后驳回。

菜单路径使用与真实文件名一致的 camelCase：

- `/training/courseManager`
- `/training/courseWorkbench`
- `/training/courseReview`
- `/training/bookingManager`

## 四、线下课程预约链路

### 1. 可预约课程

- 课程必须已启用并处于 `PUBLISHED`；
- 只允许 `OFFLINE` 和 `MIXED`；
- 纯 `ONLINE` 课程拒绝线下预约。

### 2. 用户流程

- 从课程详情进入真实预约表单；
- 填写联系人、电话、参与人数、期望时间和备注；
- 服务端按当前登录用户生成预约；
- `requestId` 保证重复提交幂等；
- 同一用户同一课程存在待确认或已确认预约时禁止重复申请；
- 用户可查看预约列表和详情；
- 待确认或已确认预约可主动取消。

### 3. 运营流程

```text
SUBMITTED
  → CONFIRMED
  → COMPLETED

SUBMITTED
  → REJECTED

SUBMITTED/CONFIRMED
  → CANCELLED（用户取消）
```

管理端可：

- 按状态和关键词查询；
- 确认预约并填写到场说明；
- 驳回并填写原因；
- 将已确认预约标记完成。

## 五、数据库模型

新增 `ldqc_training_booking`，业务状态使用 `booking_status`，继承字段 `status` 保持数值启停语义。

表包含 BladeX/TenantEntity 基础字段：

- `id`
- `tenant_id`
- `create_user`
- `create_dept`
- `create_time`
- `update_user`
- `update_time`
- `status`
- `is_deleted`

并包含：

- 用户和课程；
- 课程标题、封面、形态、类型、价格和地点快照；
- 联系人、电话、人数、期望时间和备注；
- 预约业务状态和处理说明；
- 确认、完成和取消时间；
- 用户 + `request_id` 幂等唯一键。

## 六、关键文件

### 后端

- `src/main/java/org/springblade/modules/training/service/TalentCourseWorkbenchService.java`
- `src/main/java/org/springblade/modules/training/controller/TalentCourseWorkbenchController.java`
- `src/main/java/org/springblade/modules/training/service/TrainingCourseReviewService.java`
- `src/main/java/org/springblade/modules/training/controller/TrainingCourseReviewController.java`
- `src/main/java/org/springblade/modules/trainingbooking/**`

### 小程序

- `api/talent-course-workbench.js`
- `api/training-booking.js`
- `pages_biz/talent/talent-workbench.vue`
- `pages_biz/talent/talent-course-edit.vue`
- `pages_biz/talent/talent-course-outline.vue`
- `pages_biz/training/training-booking-form.vue`
- `pages_biz/training/training-booking-list.vue`
- `pages_biz/training/training-booking-detail.vue`
- `pages_biz/training/training-detail.vue`
- `pages/user/user.vue`
- `pages.json`

### 管理端

- `src/api/training/courseReview.js`
- `src/views/training/courseReview.vue`
- `src/api/training/booking.js`
- `src/views/training/bookingManager.vue`

### SQL

- `doc/sql/training-video-course-migration.sql`
- `doc/sql/training_booking_migration.sql`
- `doc/sql/blade_base_fields_compatibility_upgrade.sql`
- `doc/sql/market_ready_phase1_schema_validation.sql`
- `src/main/java/sql/trainingcourseworkbench.menu.sql`

## 七、必须完成的真实环境验收

- MySQL 5.7 执行迁移和回滚演练；
- JDK 17 Maven 编译；
- 管理端生产构建；
- HBuilderX 与微信开发者工具编译；
- 已认证达人、普通用户和运营管理员三类账号；
- 大视频上传、FFmpeg 时长与封面处理；
- 试看、未授权、已授权播放；
- 重复请求幂等和越权访问；
- 预约提交、重复预约、确认、驳回、取消和完成。

微信课程购买、支付回调、退款和对账仍需独立支付阶段，当前不得把授权或预约包装成“已支付”。
