# 绿动全城全部整改批次 SQL 执行总清单

更新日期：2026-07-30  
适用数据库：MySQL 5.7  
适用分支：`refactor/market-ready-phase1`

## 一、结论

当前整改分支一共包含 **16 份需要核对的业务 SQL 或管理端菜单脚本**。

- 结构、业务表、字段、索引和历史数据修复：12 份；
- 管理端菜单和按钮权限种子：4 份；
- 第一批“基础契约与高风险入口收口”主要是 Controller、权限和旧旁路关闭，本身没有单独新增结构 SQL；
- 第二批、第三批、商城批次和自动审核补充批次产生了本清单中的 SQL。

不能只执行文件名中带 `migration` 的脚本，也不能把菜单 SQL 当成数据库结构迁移一起无检查执行。

## 二、批次与 SQL 对应关系

### 第一批：基础契约与高风险入口收口

本批主要完成绿动有约接口参数兼容、管理员边界和优惠券旧核销旁路关闭。

**本批没有独立新增结构 SQL。**

注意：后续第三批新增的优惠券领取规则已经依赖 `coupon_receive_rule_integrity_upgrade.sql`，所以启用最终优惠券代码时仍要执行第三批的优惠券脚本。

### 第二批：内容发布审核、长视频课程

必须或按模块执行：

1. `doc/sql/content-publish-workflow-migration.sql`
2. `doc/sql/training-video-course-migration.sql`
3. `src/main/java/sql/contentmoderation.menu.sql`
4. `src/main/java/sql/trainingcourseworkbench.menu.sql`

其中前两份是结构迁移，后两份是管理端菜单。

### 第三批：赛事、关注、场馆、优惠券、推荐

按已启用模块执行：

5. `doc/sql/competition_signup_workflow_upgrade.sql`
6. `doc/sql/follow_relation_integrity_upgrade.sql`
7. `doc/sql/venue_onboarding_upgrade.sql`
8. `doc/sql/coupon_receive_rule_integrity_upgrade.sql`
9. `doc/sql/recommend_feedback_upgrade.sql`
10. `doc/sql/venue_onboarding_menu_seed.sql`

### 商城兑换与履约批次

必须按顺序执行：

11. `doc/sql/mall-exchange-fulfillment-migration.sql`
12. `doc/sql/mall-exchange-fulfillment-data-fix.sql`
13. `src/main/java/sql/malloperation.menu.sql`

数据修复脚本必须在商城结构迁移之后执行。

### 微信文本与媒体自动审核补充批次

必须按顺序补充：

14. `doc/sql/comment_audit_fields_compatibility_upgrade.sql`
15. `doc/sql/content_audit_retry_upgrade.sql`
16. `doc/sql/dynamic_content_auto_audit_upgrade.sql`

并重新执行或确认已经执行最新版本：

- `src/main/java/sql/contentmoderation.menu.sql`

自动审核批次复用第二批的 `content-publish-workflow-migration.sql`，不要遗漏该前置脚本。

## 三、推荐统一执行顺序

在一个全新测试库，或当前库尚未执行过本轮任何迁移时，推荐按以下顺序：

```text
01 content-publish-workflow-migration.sql
02 comment_audit_fields_compatibility_upgrade.sql
03 content_audit_retry_upgrade.sql
04 dynamic_content_auto_audit_upgrade.sql
05 training-video-course-migration.sql
06 competition_signup_workflow_upgrade.sql
07 follow_relation_integrity_upgrade.sql
08 venue_onboarding_upgrade.sql
09 coupon_receive_rule_integrity_upgrade.sql
10 mall-exchange-fulfillment-migration.sql
11 mall-exchange-fulfillment-data-fix.sql
12 recommend_feedback_upgrade.sql
13 contentmoderation.menu.sql
14 trainingcourseworkbench.menu.sql
15 venue_onboarding_menu_seed.sql
16 malloperation.menu.sql
```

说明：

- 第 1～4 步构成动态与评论自动审核完整链路；
- 第 10、11 步必须相邻并保持先后顺序；
- 第 13～16 步是菜单种子，建议在对应管理端页面部署后执行；
- 不使用某个业务模块时，可以暂缓该模块 SQL，但对应页面和接口也不能视为可用。

## 四、各脚本用途与执行要求

| 序号 | SQL | 模块 | 执行要求 | 主要影响 |
|---:|---|---|---|---|
| 1 | `content-publish-workflow-migration.sql` | 社区动态 | 必须 | 动态审核字段、正式发布时间、媒体处理状态、达人来源唯一索引 |
| 2 | `comment_audit_fields_compatibility_upgrade.sql` | 评论审核 | 自动审核必需 | 社区/新闻评论审核状态、说明、时间、任务ID和公开查询索引 |
| 3 | `content_audit_retry_upgrade.sql` | 审核任务 | 自动审核必需 | 创建/补齐 `content_audit_task`、重试字段和调度索引 |
| 4 | `dynamic_content_auto_audit_upgrade.sql` | 微信媒体审核 | 自动审核必需 | 动态任务聚合索引和微信 `trace_id` 回调索引 |
| 5 | `training-video-course-migration.sql` | 长视频课程 | 启用课程必需 | 课程发布字段、章节、课时、授权、学习进度 |
| 6 | `competition_signup_workflow_upgrade.sql` | 赛事报名 | 启用新报名流程必需 | 报名窗口、订单快照、金额、状态、幂等和名额相关索引 |
| 7 | `follow_relation_integrity_upgrade.sql` | 关注/粉丝 | 启用新关注服务必需 | 去自关注、去重复关系、有效唯一键、重新校准关注数和粉丝数 |
| 8 | `venue_onboarding_upgrade.sql` | 场馆入驻 | 启用入驻必需 | 场馆运营者字段、来源申请字段、场馆入驻申请表和索引 |
| 9 | `coupon_receive_rule_integrity_upgrade.sql` | 优惠券领取 | 启用新领券流程必需 | 认证要求、领取窗口、领取幂等、限领和库存查询索引 |
| 10 | `mall-exchange-fulfillment-migration.sql` | 商城 | 启用新商城必需 | 商品运营字段、订单快照、三类履约、地址/物流/领取码和索引 |
| 11 | `mall-exchange-fulfillment-data-fix.sql` | 商城历史数据 | 第 10 步后必须 | 规范商品类型、履约类型、限兑、地址要求和订单履约状态 |
| 12 | `recommend_feedback_upgrade.sql` | 推荐反馈 | 启用行为推荐必需 | 新建曝光、点击、停留、视频完成、不感兴趣反馈表 |
| 13 | `contentmoderation.menu.sql` | 内容运营 | 管理端入口 | 内容人工复核、自动审核异常和按钮权限 |
| 14 | `trainingcourseworkbench.menu.sql` | 课程运营 | 管理端入口 | 课程管理、章节课时、发布下架和播放授权菜单 |
| 15 | `venue_onboarding_menu_seed.sql` | 场馆运营 | 管理端入口 | 场馆入驻查看、通过和驳回菜单 |
| 16 | `malloperation.menu.sql` | 商城运营 | 管理端入口 | 商品管理、发货、到店领取、虚拟权益和取消退款菜单 |

## 五、会修改历史数据的脚本

以下脚本不仅建字段或索引，还会修改历史数据，必须先备份并核对受影响行数。

### 1. 内容发布迁移

`content-publish-workflow-migration.sql` 会：

- 给历史已发布动态补 `publish_time`；
- 视频没有可用封面时将 `media_process_status` 初始化为 `PROCESSING`；
- 给达人内容增加来源唯一索引。

### 2. 评论兼容迁移

`comment_audit_fields_compatibility_upgrade.sql` 会把审核状态为空的历史评论初始化为已通过 `1`，用于保持升级前可见性。

### 3. 审核重试迁移

`content_audit_retry_upgrade.sql` 会：

- 初始化历史异常任务尝试次数；
- 给状态 3 且无重试时间的任务设置立即重试；
- 将尝试次数大于等于 5 的任务转为人工待办状态 4。

### 4. 赛事报名迁移

`competition_signup_workflow_upgrade.sql` 会：

- 规范赛事报名窗口和支付方式；
- 仅将没有 `order_no` 的旧报名迁移为 `LEGACY_REVIEW`；
- 不会把新流程已经生成订单号的订单再次改为历史待核对状态。

历史报名不能自动认定为真实已支付，需要运营人工核对。

### 5. 关注关系迁移

`follow_relation_integrity_upgrade.sql` 会：

- 删除有效自关注；
- 同一用户对同一目标只保留一条有效关系；
- 根据有效关系重新计算 `blade_user.follow_count` 和 `fan_count`。

### 6. 优惠券迁移

`coupon_receive_rule_integrity_upgrade.sql` 会：

- 将旧 `ext_json.receive_auth_required` 迁移到显式字段；
- 规范限领、成长等级和绿豆成本；
- 清理同一用户同一请求号的重复有效领取日志，只保留最早记录。

### 7. 商城迁移和数据修复

商城两份脚本会：

- 给历史订单补商品名称、主图、单件绿豆和履约快照；
- 根据旧 `delivery_status` 映射履约状态；
- 规范空商品类型、履约类型、单次限兑和地址要求。

## 六、执行前必须检查

### 1. 数据库和版本

```sql
SELECT DATABASE() AS current_database, VERSION() AS mysql_version;
```

必须确认连接的是测试库，且数据库兼容 MySQL 5.7。

### 2. 建议备份表

至少备份：

```text
t_img_detail
ldqc_talent_post
t_comment
n_news_comment
content_audit_task
ldqc_training
ldqc_training_chapter
ldqc_training_lesson
ldqc_training_access
ldqc_training_progress
ldqc_competition
ldqc_competition_signup
t_follow
blade_user
ldqc_venue
ldqc_venue_apply
coupon_template
coupon_receive_log
user_coupon
mall_product
mall_exchange_order
ldqc_recommend_feedback
blade_menu
```

不存在的新表可以忽略备份，但应记录执行前不存在。

### 3. 达人来源内容重复检查

```sql
SELECT source_content_id, COUNT(*) AS duplicate_count
FROM ldqc_talent_post
WHERE source_content_id IS NOT NULL
GROUP BY source_content_id
HAVING COUNT(*) > 1;
```

结果必须为空，否则 `uk_talent_post_source_content` 唯一索引可能失败。

### 4. 商城请求号重复检查

```sql
SELECT user_id, request_id, COUNT(*) AS duplicate_count
FROM mall_exchange_order
WHERE request_id IS NOT NULL
  AND request_id <> ''
GROUP BY user_id, request_id
HAVING COUNT(*) > 1;
```

结果非空时，先核对重复订单，不要直接创建 `uk_mall_order_user_request`。

### 5. 菜单 ID 与 code 冲突检查

```sql
SELECT id, parent_id, code, name, path, is_deleted
FROM blade_menu
WHERE id BETWEEN 205150050 AND 205150053
   OR id BETWEEN 205160001 AND 205160008
   OR id BETWEEN 205170000 AND 205170025
   OR id BETWEEN 205180000 AND 205180024
   OR code IN (
     'venueApplyAudit',
     'contentModeration',
     'contentAuditExceptions',
     'trainingOperation',
     'trainingCourseManager',
     'trainingCourseWorkbench',
     'mallOperation',
     'mallProductManager',
     'mallFulfillmentWorkbench'
   );
```

如果同一 ID 已属于其他业务，必须先重新规划菜单 ID，不能直接执行。

## 七、执行后验证

### 1. 检查本轮核心表

```sql
SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN (
    'content_audit_task',
    'ldqc_training_chapter',
    'ldqc_training_lesson',
    'ldqc_training_access',
    'ldqc_training_progress',
    'ldqc_venue_apply',
    'ldqc_recommend_feedback'
  )
ORDER BY TABLE_NAME;
```

完整启用全部模块时应返回 7 张表。

### 2. 检查审核任务

```sql
SELECT biz_type, audit_status, COUNT(*) AS task_count
FROM content_audit_task
WHERE is_deleted = 0
GROUP BY biz_type, audit_status
ORDER BY biz_type, audit_status;
```

没有测试数据时返回空结果正常。

### 3. 检查赛事历史迁移

```sql
SELECT order_status, COUNT(*) AS order_count
FROM ldqc_competition_signup
WHERE is_deleted = 0
GROUP BY order_status;
```

历史旧记录应为 `LEGACY_REVIEW`；新流程订单不能被重复迁移覆盖。

### 4. 检查关注计数

```sql
SELECT
  SUM(CASE WHEN f.uid = u.id AND f.is_deleted = 0 THEN 1 ELSE 0 END) AS relation_follow_count,
  u.follow_count
FROM blade_user u
LEFT JOIN t_follow f ON f.uid = u.id
WHERE u.is_deleted = 0
GROUP BY u.id, u.follow_count
HAVING relation_follow_count <> u.follow_count
LIMIT 20;
```

结果应为空。

### 5. 检查菜单

执行菜单脚本后，再在角色管理中授权。菜单数据存在不代表角色已经获得权限。

## 八、可暂缓的情况

- 暂时不上长视频课程：可暂缓第 5、14 步，但课程页面和接口不能上线；
- 暂时不上场馆入驻：可暂缓第 8、15 步；
- 暂时不上积分商城：可暂缓第 10、11、16 步；
- 暂时不启用推荐行为反馈：可暂缓第 12 步，但后端和小程序必须关闭反馈写入；
- 暂时不启用赛事报名：可暂缓第 6 步，不能开放新报名页面；
- 动态发布、评论和微信自动审核已经进入当前代码主链路，因此第 1～4、13 步不建议暂缓。

## 九、不属于 SQL 的部署项

以下内容必须通过环境变量或部署配置完成，不能写入数据库：

```yaml
WECHAT:
  app-id: ${WECHAT_APP_ID}
  app-secret: ${WECHAT_APP_SECRET}
  message-token: ${WECHAT_MESSAGE_TOKEN}
```

还需要：

- 微信后台配置媒体审核回调；
- MinIO/OSS 图片和视频封面允许微信服务器公网下载；
- FFmpeg/FFprobe 安装和路径配置；
- 菜单执行后给角色授权；
- HBuilderX、微信开发者工具和真机联调。

## 十、生产执行原则

1. 所有脚本先在测试库逐份执行；
2. 每份执行后记录开始时间、结束时间、影响行数和验证结果；
3. 数据修复脚本不要与结构迁移一次性无检查提交；
4. 发现唯一索引冲突时先处理重复数据，不要删除索引语句绕过；
5. 测试库完成接口与管理端联调后，才制定生产变更窗口；
6. 当前 Draft PR 不因为 SQL 文件存在就视为数据库验收通过。
