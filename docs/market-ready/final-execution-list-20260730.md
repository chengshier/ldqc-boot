# market-ready-phase1 最终执行文件清单

更新日期：2026-07-30  
适用分支：三个仓库均为 `refactor/market-ready-phase1`

> 本清单用于把当前分支部署到测试环境并完成真实验收。请勿直接合并 `master`，也不要跳过数据库备份、迁移验证、管理端菜单、微信开发者工具和多角色联调。

---

## 一、执行前准备

### 1. 代码分支

确认以下仓库均检出：

- `chengshier/ldqc-boot` → `refactor/market-ready-phase1`
- `chengshier/ldqc-vue` → `refactor/market-ready-phase1`
- `chengshier/ldqc-wx` → `refactor/market-ready-phase1`

禁止使用 `master` 生成本轮测试包。

### 2. 数据库备份

至少备份：

```text
ldqc_training
ldqc_training_chapter
ldqc_training_lesson
ldqc_training_access
ldqc_training_progress
ldqc_training_booking（若历史库已存在）
ldqc_recommend_feedback
competition_signup
content_audit_task
coupon_template
coupon_receive_log
coupon_verifier_scope
coupon_verify_log
user_coupon
mall_product
mall_exchange_order
venue_apply
blade_menu
blade_role_menu
```

记录备份时间、数据库名、备份文件和恢复命令。

### 3. 环境要求

- MySQL 5.7；
- JDK 17；
- Maven 可访问 BladeX 私有仓库；
- Node.js 20 与项目对应 Yarn；
- HBuilderX；
- 微信开发者工具；
- FFmpeg 与 FFprobe；
- 可公网访问的 MinIO/OSS 媒体域名；
- 普通用户、达人、场馆运营者、核销员、运营管理员测试账号。

---

## 二、数据库 SQL 执行顺序

以下顺序按“业务结构 → 运行时兼容 → BladeX 基础字段 → 数据修复 → 菜单 → 验证”执行。

### A. 核心业务结构

```text
1. doc/sql/content-publish-workflow-migration.sql
2. doc/sql/content_audit_retry_upgrade.sql
3. doc/sql/dynamic_content_auto_audit_upgrade.sql
4. doc/sql/recommend_feedback_upgrade.sql
5. doc/sql/follow_relation_integrity_upgrade.sql
6. doc/sql/competition_signup_workflow_upgrade.sql
7. doc/sql/training-video-course-migration.sql
8. doc/sql/training_booking_migration.sql
9. doc/sql/venue_onboarding_upgrade.sql
10. doc/sql/coupon_receive_rule_integrity_upgrade.sql
11. doc/sql/coupon-security-migration.sql
12. doc/sql/mall-exchange-fulfillment-migration.sql
```

说明：

- 第 7 份建立课程章节、课时、播放授权和学习进度；
- 第 8 份建立真实线下/混合课程预约表；
- 第 11 份建立核销员范围表；
- 第 12 份建立商城履约字段和订单结构。

### B. 运行时兼容和历史库修复

```text
13. doc/sql/comment_audit_fields_compatibility_upgrade.sql
14. doc/sql/coupon_runtime_compatibility_fix.sql
15. doc/sql/coupon_verify_log_runtime_fix.sql
16. doc/sql/mall_product_runtime_compatibility_fix.sql
17. doc/sql/mall-exchange-fulfillment-data-fix.sql
18. doc/sql/mall_exchange_order_status_runtime_fix.sql
```

重点：

- `coupon_verify_log.verify_status` 必须存在；
- `user_coupon.status` 恢复数值启停语义，券业务状态使用 `coupon_status`；
- `mall_exchange_order.status` 恢复数值启停语义，业务订单状态使用 `order_status`；
- `mall_product.sold_qty` 等实体运行字段必须存在。

### C. BladeX 基础字段统一补丁

```text
19. doc/sql/blade_base_fields_compatibility_upgrade.sql
```

该脚本核对和补充：

```text
id
 tenant_id
create_user
create_dept
create_time
update_user
update_time
status
is_deleted
```

其中 `status` 必须为数值数据状态，业务枚举必须放独立字段：

- `coupon_status`
- `verify_status`
- `order_status`
- `publish_status`
- `media_process_status`
- `booking_status`
- 其他业务专属状态字段。

### D. 历史数据清理

先人工检查脚本中的查询结果，再执行更新部分：

```text
20. doc/sql/market_ready_phase1_data_cleanup.sql
```

不得把来源不明的文件名、文档名或上传记录直接保留为课程标题。异常课程数据应先导出留档，再由运营确认删除、下架或修正。

### E. 管理端菜单和权限种子

```text
21. src/main/java/sql/contentmoderation.menu.sql
22. src/main/java/sql/venue_onboarding_menu_seed.sql
23. src/main/java/sql/malloperation.menu.sql
24. src/main/java/sql/trainingcourseworkbench.menu.sql
25. doc/sql/market_ready_admin_route_fix.sql
```

第 24 份包含：

- 课程管理；
- 课程内容工作台；
- 课程审核；
- 课程预约；
- 章节、课时、发布、下架、授权；
- 审核通过、审核驳回；
- 预约确认、预约驳回、预约完成。

执行后必须在角色管理中授予实际运营角色菜单和按钮权限。

### F. 数据库验证

```text
26. doc/sql/market_ready_phase1_schema_validation.sql
```

验收要求：

- 基础字段矩阵中已安装业务表九项均为 `1`；
- `coupon_verify_log.verify_status` 存在；
- `ldqc_training_booking.booking_status` 存在；
- 所有继承字段 `status` 为数值类型；
- 业务状态误用检查结果均为 `0`；
- 菜单路径与前端真实文件名大小写一致。

---

## 三、后端构建和部署

仓库：`ldqc-boot`

### 1. 配置检查

必须配置并验证：

```text
WECHAT_APP_ID
WECHAT_APP_SECRET
WECHAT_MESSAGE_TOKEN
数据库连接
Redis
MinIO/OSS
FFmpeg
FFprobe
正式媒体公网域名
```

媒体审核回调：

```text
/blade-contentaudit/wechat/media-callback
```

### 2. 构建命令

```bash
mvn -B -U -DskipTests clean package
```

不得仅以 IDE 无红线代替 Maven 构建。

### 3. 部署后检查

至少检查接口：

```text
POST /blade-recommend/feedback
GET  /blade-venue/venue/mobile/page
GET  /blade-training/training/mobile-page
GET  /blade-training/training/mobile-detail
POST /blade-training/talent-workbench/settings
POST /blade-training/talent-workbench/lesson/save
POST /blade-training/talent-workbench/submit-review
GET  /blade-training/course-review/page
POST /blade-training/course-review/approve
POST /blade-training/course-review/reject
POST /blade-training/booking/submit
GET  /blade-training/booking/my-page
POST /blade-training/booking/cancel
GET  /blade-training/booking/admin-page
POST /blade-training/booking/admin-confirm
POST /blade-training/booking/admin-reject
POST /blade-training/booking/admin-complete
```

重启后端后检查启动日志、MyBatis 字段映射、SQL 异常和定时任务。

---

## 四、管理端构建和部署

仓库：`ldqc-vue`

### 1. 构建

使用项目锁定的包管理器：

```bash
yarn install --frozen-lockfile
yarn build:prod
```

### 2. 部署后操作

- 发布新的 `dist`；
- 清理浏览器缓存；
- 退出并重新登录管理端；
- 清理 BladeX 菜单/权限缓存；
- 给测试角色重新授权。

### 3. 必须打开验证的页面

```text
内容人工复核
内容自动审核异常
场馆入驻审核
商城商品管理
商城履约工作台
课程管理
课程内容工作台
课程审核
课程预约
```

`apple-mobile-web-app-capable` 弃用提示不是页面空白原因。若仍空白，必须查看：

- 浏览器 Console JavaScript 错误；
- Network 中菜单、权限和页面初始化接口；
- `blade_menu.path`；
- 当前部署的前端 commit；
- 当前账号菜单和按钮权限。

---

## 五、小程序编译和发布

仓库：`ldqc-wx`

### 1. 编译前

- 确认运行的不是缓存中的 `master`；
- 确认生产 API 域名；
- 清理 HBuilderX 和微信开发者工具缓存；
- 确认图片、视频和上传域名已加入微信合法域名。

### 2. 编译

当前仓库没有可直接替代 HBuilderX 的完整 uni-app CLI 发布脚本，因此必须：

1. 使用 HBuilderX 编译微信小程序；
2. 使用微信开发者工具重新导入编译产物；
3. 检查 WXML、easycom、分包、上传和播放器；
4. 使用真机而非只看模拟器。

### 3. 必须验证的页面

```text
首页推荐
关注动态
体育达人
赛事
体育培训
周边场馆
场馆分类 Tab 和距离筛选
我的页面
作品 / 喜欢 / 收藏
达人工作台
达人课程编辑
章节课时与长视频上传
课程详情与试看
课程预约表单
我的课程预约
课程预约详情
优惠券详情与核销
商城商品和兑换订单
```

---

## 六、多角色 UAT 顺序

### 1. 普通用户

- 登录、兴趣设置；
- 推荐曝光/点击/不感兴趣；
- 关注、点赞、喜欢、收藏；
- 赛事免费报名；
- 线下课程预约、查看、取消；
- 优惠券领取与动态码；
- 商城兑换。

### 2. 已认证达人

- “我的”显示达人工作台；
- 新建课程；
- 上传封面；
- 创建章节和视频课时；
- 配置试看；
- 视频处理成功/失败重试；
- 提交审核；
- 无法越权修改其他达人课程；
- 无法直接发布或授权其他用户。

### 3. 运营管理员

- 审核达人课程并通过/驳回；
- 处理课程预约；
- 管理课程章节和播放授权；
- 内容自动审核异常；
- 场馆入驻；
- 商城履约；
- 优惠券核销范围。

### 4. 场馆运营者与核销员

- 只看到本人场馆工作台；
- 核销员只能核销授权范围内优惠券；
- 越权请求被后端拒绝。

---

## 七、暂不标记完成的外部能力

以下内容没有真实基础设施或真实环境验证前，不得标记为完成：

- 微信课程购买、预支付、支付回调、退款和对账；
- 付费赛事支付；
- 场馆在线订场、时段库存、支付和退款；
- 微信文本/媒体审核真实 OpenID 和回调；
- 正式 OSS/MinIO 与微信服务器公网访问；
- FFmpeg 长视频真实处理；
- 高并发库存、绿豆、名额和预约测试；
- 真机弱网、后台恢复和长时间视频播放。

当前课程预约为真实预约状态机，但预约记录和价格快照不代表支付成功。

---

## 八、最终上线准入

同时满足以下条件后，三个 Draft PR 才可转为 Ready for review：

- 全部 SQL 和验证 SQL 在 MySQL 5.7 测试库通过；
- 后端 Maven 构建通过；
- 管理端生产构建通过；
- HBuilderX 与微信开发者工具编译通过；
- 普通用户、达人、运营管理员、场馆运营者和核销员完成 UAT；
- 关键失败、重复、越权和回滚场景通过；
- 无 P0/P1 未关闭问题；
- 本清单中的延期能力未被页面伪装为已开放。
