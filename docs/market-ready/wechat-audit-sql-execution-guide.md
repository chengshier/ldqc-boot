# 微信文本与媒体自动审核 SQL 执行清单

更新日期：2026-07-30
适用数据库：MySQL 5.7
适用分支：`refactor/market-ready-phase1`

## 一、结论

本次微信文本与媒体自动审核不是只执行一份索引脚本，而是需要按顺序执行：

1. `doc/sql/content-publish-workflow-migration.sql`
2. `doc/sql/content_audit_retry_upgrade.sql`
3. `doc/sql/dynamic_content_auto_audit_upgrade.sql`
4. `src/main/java/sql/contentmoderation.menu.sql`

其中前三份是业务结构、字段与索引脚本；第四份是管理端菜单和按钮权限种子。

## 二、执行顺序

### 第 1 步：社区动态发布审核字段

文件：

```text
doc/sql/content-publish-workflow-migration.sql
```

作用：

- 给 `t_img_detail` 增加 `audit_reason`；
- 给 `t_img_detail` 增加 `audit_time`；
- 给 `t_img_detail` 增加 `audit_user_id`；
- 给 `t_img_detail` 增加 `publish_time`；
- 给 `t_img_detail` 增加 `media_process_status`；
- 给 `ldqc_talent_post` 增加 `source_content_id`；
- 增加动态公开查询、用户状态查询和达人来源内容唯一索引；
- 修复历史已发布动态的发布时间和媒体处理状态。

这一步是自动审核发布、驳回、下架和视频封面状态的基础，不能只执行后面的审核任务表脚本。

### 第 2 步：审核任务表与重试字段

文件：

```text
doc/sql/content_audit_retry_upgrade.sql
```

作用：

- 创建或补齐 `content_audit_task`；
- 支持动态文案 `IMG_DETAIL_TEXT`；
- 支持动态图片或视频封面 `IMG_DETAIL_MEDIA`；
- 支持社区评论 `TREND_COMMENT`；
- 支持新闻评论 `NEWS_COMMENT`；
- 增加审核状态、微信追踪号、结果码、结果说明；
- 增加尝试次数、下次重试时间和最终审核时间；
- 增加重试调度索引；
- 将历史异常任务初始化为可重试或人工处理状态。

如果数据库中没有 `content_audit_task`，这份脚本负责建表；如果已有旧表，则负责补列和补索引。

### 第 3 步：动态审核聚合与微信回调索引

文件：

```text
doc/sql/dynamic_content_auto_audit_upgrade.sql
```

作用：

- 增加 `idx_content_audit_biz_status`；
- 增加 `idx_content_audit_provider_trace`；
- 优化同一条动态的文案和多媒体任务聚合；
- 支持微信异步媒体回调按 `provider_trace_id` 查找任务；
- 优化管理端异常任务查询。

这份脚本依赖第 1、2 步，不能单独先执行。

### 第 4 步：管理端菜单与按钮

文件：

```text
src/main/java/sql/contentmoderation.menu.sql
```

作用：

- 创建“内容审核”菜单；
- 创建查看详情、人工通过、人工驳回和内容下架按钮；
- 创建“自动审核异常”菜单；
- 创建立即重试和人工处理按钮。

这份脚本只创建菜单数据，不会自动给所有角色授权。执行后还要在角色管理中给内容运营或审核员授予菜单和按钮权限。

## 三、执行前检查

### 1. 确认当前数据库

```sql
SELECT DATABASE();
SELECT VERSION();
```

确认连接的是测试库，并且版本为 MySQL 5.7。

### 2. 备份核心表

至少备份：

```text
t_img_detail
ldqc_talent_post
content_audit_task
blade_menu
```

### 3. 检查达人来源内容是否重复

第 1 份脚本会创建 `ldqc_talent_post(source_content_id)` 唯一索引。执行前先检查：

```sql
SELECT source_content_id, COUNT(*) AS duplicate_count
FROM ldqc_talent_post
WHERE source_content_id IS NOT NULL
GROUP BY source_content_id
HAVING COUNT(*) > 1;
```

查询结果必须为空；否则应先处理重复数据，再创建唯一索引。

### 4. 检查菜单 ID 和 code 冲突

```sql
SELECT id, parent_id, code, name, path, is_deleted
FROM blade_menu
WHERE id IN (
  205160001,205160002,205160003,205160004,
  205160005,205160006,205160007,205160008
)
OR code IN (
  'contentModeration',
  'contentModeration_view',
  'contentModeration_pass',
  'contentModeration_reject',
  'contentModeration_offline',
  'contentAuditExceptions',
  'contentAuditExceptions_retry',
  'contentAuditExceptions_resolve'
);
```

如果存在同 ID 但属于其他业务的菜单，不要直接执行菜单脚本，应先重新规划菜单 ID。

## 四、推荐执行方式

建议在 Navicat、DataGrip 或 MySQL 命令行中逐份执行，不要把四份文件一次性无检查地粘贴到生产库。

执行顺序：

```text
content-publish-workflow-migration.sql
        ↓
content_audit_retry_upgrade.sql
        ↓
dynamic_content_auto_audit_upgrade.sql
        ↓
contentmoderation.menu.sql
```

每执行一份，先检查是否报错，再执行下一份。

## 五、执行后验证

### 1. 验证动态审核字段

```sql
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_img_detail'
  AND COLUMN_NAME IN (
    'audit_reason',
    'audit_time',
    'audit_user_id',
    'publish_time',
    'media_process_status'
  )
ORDER BY COLUMN_NAME;
```

应返回 5 行。

### 2. 验证达人来源字段

```sql
SELECT COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'ldqc_talent_post'
  AND COLUMN_NAME = 'source_content_id';
```

### 3. 验证审核任务表字段

```sql
SELECT COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'content_audit_task'
  AND COLUMN_NAME IN (
    'biz_type',
    'biz_id',
    'user_id',
    'content_snapshot',
    'audit_status',
    'provider_trace_id',
    'result_code',
    'result_message',
    'attempt_count',
    'next_retry_time',
    'audit_time'
  )
ORDER BY COLUMN_NAME;
```

应返回 11 行。

### 4. 验证全部索引

```sql
SELECT TABLE_NAME, INDEX_NAME,
       GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns_in_index
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND (
    (TABLE_NAME = 't_img_detail' AND INDEX_NAME IN (
      'idx_img_detail_public',
      'idx_img_detail_user_status'
    ))
    OR
    (TABLE_NAME = 'ldqc_talent_post' AND INDEX_NAME = 'uk_talent_post_source_content')
    OR
    (TABLE_NAME = 'content_audit_task' AND INDEX_NAME IN (
      'idx_content_audit_biz',
      'idx_content_audit_retry',
      'idx_content_audit_user',
      'idx_content_audit_biz_status',
      'idx_content_audit_provider_trace'
    ))
  )
GROUP BY TABLE_NAME, INDEX_NAME
ORDER BY TABLE_NAME, INDEX_NAME;
```

### 5. 验证菜单

```sql
SELECT id, parent_id, code, name, path, category, action, is_deleted
FROM blade_menu
WHERE code IN (
  'contentModeration',
  'contentModeration_view',
  'contentModeration_pass',
  'contentModeration_reject',
  'contentModeration_offline',
  'contentAuditExceptions',
  'contentAuditExceptions_retry',
  'contentAuditExceptions_resolve'
)
ORDER BY id;
```

应返回 8 行。

### 6. 验证审核任务状态

```sql
SELECT biz_type, audit_status, COUNT(*) AS task_count
FROM content_audit_task
WHERE is_deleted = 0
GROUP BY biz_type, audit_status
ORDER BY biz_type, audit_status;
```

刚执行 SQL、尚未发布测试动态时，返回空结果是正常的。

## 六、不属于 SQL 的必要配置

以下内容不能通过 SQL 完成：

```yaml
WECHAT:
  app-id: ${WECHAT_APP_ID}
  app-secret: ${WECHAT_APP_SECRET}
  message-token: ${WECHAT_MESSAGE_TOKEN}
```

还必须在微信小程序后台配置媒体审核回调地址：

```text
https://正式域名/api/blade-contentaudit/wechat/media-callback
```

并保证动态图片和视频封面是微信服务器可以直接下载的公网 HTTP/HTTPS 地址。

## 七、是否可以重复执行

- `content-publish-workflow-migration.sql`：字段和索引大部分支持重复执行，但唯一索引仍要求历史数据无重复；
- `content_audit_retry_upgrade.sql`：支持旧表补列和重复执行；
- `dynamic_content_auto_audit_upgrade.sql`：按索引名判断，支持重复执行；
- `contentmoderation.menu.sql`：使用 `ON DUPLICATE KEY UPDATE`，原则上可重复执行，但必须先确认菜单 ID 和 code 未被其他业务占用。

## 八、当前未包含的 SQL

本次微信自动审核不需要新增：

- 微信 AppID/AppSecret 配置表；
- 收费审核服务配置表；
- 视频逐帧审核表；
- OCR 审核表；
- 新的动态主表。

微信密钥和回调 Token 必须放在环境变量或密钥管理中，不能写入 SQL 或 Git 仓库。
