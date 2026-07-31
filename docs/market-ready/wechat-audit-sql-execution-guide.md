# 微信文本与媒体自动审核 SQL 执行清单

更新日期：2026-07-30  
适用数据库：MySQL 5.7  
适用分支：`refactor/market-ready-phase1`

## 一、最终结论

本次微信文本与媒体自动审核需要核对并按顺序执行 **5 份 SQL**：

1. `doc/sql/content-publish-workflow-migration.sql`
2. `doc/sql/comment_audit_fields_compatibility_upgrade.sql`
3. `doc/sql/content_audit_retry_upgrade.sql`
4. `doc/sql/dynamic_content_auto_audit_upgrade.sql`
5. `src/main/java/sql/contentmoderation.menu.sql`

其中：

- 第 1、3、4 份是动态自动审核主链路必需脚本；
- 第 2 份是评论审核字段兼容脚本，已按幂等方式补齐，建议统一执行；
- 第 5 份是管理端菜单和按钮权限种子，不影响后端接口启动，但不执行则运营人员没有正常入口。

## 二、执行顺序与作用

### 第 1 步：动态发布与人工审核字段

文件：

```text
doc/sql/content-publish-workflow-migration.sql
```

作用：

- 给 `t_img_detail` 增加：
  - `audit_reason`
  - `audit_time`
  - `audit_user_id`
  - `publish_time`
  - `media_process_status`
- 给 `ldqc_talent_post` 增加 `source_content_id`；
- 增加动态公开查询、用户状态查询和达人来源内容唯一索引；
- 修复历史已发布动态的发布时间和媒体处理状态。

这一步是自动发布、自动驳回、人工复核、内容下架和视频封面处理的基础。

### 第 2 步：评论审核字段兼容

文件：

```text
doc/sql/comment_audit_fields_compatibility_upgrade.sql
```

作用：

- 给 `t_comment` 补齐：
  - `audit_status`
  - `audit_reason`
  - `audit_time`
  - `audit_task_id`
- 给 `n_news_comment` 补齐：
  - `comment_status`
  - `audit_reason`
  - `audit_time`
  - `audit_task_id`
- 增加评论公开查询和审核任务回查索引；
- 历史评论状态为空时初始化为已通过 `1`，避免升级后历史评论全部不可见。

这些字段虽然在代码实体中已经存在，但之前的自动审核执行文档没有明确对应迁移，因此本次补充为独立兼容脚本。

### 第 3 步：审核任务表与自动重试

文件：

```text
doc/sql/content_audit_retry_upgrade.sql
```

作用：

- 创建或补齐 `content_audit_task`；
- 支持四类任务：
  - `IMG_DETAIL_TEXT`
  - `IMG_DETAIL_MEDIA`
  - `TREND_COMMENT`
  - `NEWS_COMMENT`
- 增加：
  - 微信 `provider_trace_id`
  - `result_code`
  - `result_message`
  - `attempt_count`
  - `next_retry_time`
  - `audit_time`
- 增加重试调度索引；
- 初始化历史异常任务；
- 超过 5 次的历史重试任务转人工待办。

如果数据库没有 `content_audit_task`，该脚本负责建表；如果已有旧表，则负责补字段和索引。

### 第 4 步：动态任务聚合与媒体回调索引

文件：

```text
doc/sql/dynamic_content_auto_audit_upgrade.sql
```

作用：

- 增加 `idx_content_audit_biz_status`；
- 增加 `idx_content_audit_provider_trace`；
- 支持同一动态的文案和多张图片/视频封面任务聚合；
- 支持微信异步媒体回调按 `provider_trace_id` 查找任务；
- 优化管理端异常任务列表。

这份脚本依赖第 3 步中的 `content_audit_task`，不能提前执行。

### 第 5 步：管理端菜单和按钮

文件：

```text
src/main/java/sql/contentmoderation.menu.sql
```

作用：

- 创建“内容审核”菜单；
- 创建查看详情、人工通过、人工驳回、内容下架按钮；
- 创建“自动审核异常”菜单；
- 创建立即重试、人工处理按钮。

注意：该脚本只创建 `blade_menu` 数据，不会自动给角色授权。执行后还需要在角色管理中给运营人员或审核员授权。

## 三、推荐执行顺序

```text
content-publish-workflow-migration.sql
        ↓
comment_audit_fields_compatibility_upgrade.sql
        ↓
content_audit_retry_upgrade.sql
        ↓
dynamic_content_auto_audit_upgrade.sql
        ↓
contentmoderation.menu.sql
```

建议逐份执行，每份成功后再执行下一份，不要直接把五份脚本一次性粘贴到生产库。

## 四、执行前检查

### 1. 确认数据库与版本

```sql
SELECT DATABASE();
SELECT VERSION();
```

确认连接的是测试库，并且版本为 MySQL 5.7。

### 2. 备份表

至少备份：

```text
t_img_detail
ldqc_talent_post
t_comment
n_news_comment
content_audit_task
blade_menu
```

### 3. 检查达人来源内容重复

第 1 份脚本会创建唯一索引：

```sql
SELECT source_content_id, COUNT(*) AS duplicate_count
FROM ldqc_talent_post
WHERE source_content_id IS NOT NULL
GROUP BY source_content_id
HAVING COUNT(*) > 1;
```

结果必须为空，否则先处理重复数据。

### 4. 检查菜单冲突

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

如果同一 ID 已被其他业务占用，不要直接执行菜单脚本。

## 五、执行后验证

### 1. 动态字段

```sql
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_img_detail'
  AND COLUMN_NAME IN (
    'audit_reason','audit_time','audit_user_id',
    'publish_time','media_process_status'
  )
ORDER BY COLUMN_NAME;
```

应返回 5 行。

### 2. 评论审核字段

```sql
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND (
    (TABLE_NAME = 't_comment' AND COLUMN_NAME IN (
      'audit_status','audit_reason','audit_time','audit_task_id'
    ))
    OR
    (TABLE_NAME = 'n_news_comment' AND COLUMN_NAME IN (
      'comment_status','audit_reason','audit_time','audit_task_id'
    ))
  )
ORDER BY TABLE_NAME, COLUMN_NAME;
```

应返回 8 行。

### 3. 审核任务表字段

```sql
SELECT COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'content_audit_task'
  AND COLUMN_NAME IN (
    'biz_type','biz_id','user_id','content_snapshot',
    'audit_status','provider_trace_id','result_code',
    'result_message','attempt_count','next_retry_time','audit_time'
  )
ORDER BY COLUMN_NAME;
```

应返回 11 行。

### 4. 关键索引

```sql
SELECT TABLE_NAME, INDEX_NAME,
       GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns_in_index
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND INDEX_NAME IN (
    'idx_img_detail_public',
    'idx_img_detail_user_status',
    'uk_talent_post_source_content',
    'idx_comment_public',
    'idx_comment_audit_task',
    'idx_news_comment_public',
    'idx_news_comment_audit_task',
    'idx_content_audit_biz',
    'idx_content_audit_retry',
    'idx_content_audit_user',
    'idx_content_audit_biz_status',
    'idx_content_audit_provider_trace'
  )
GROUP BY TABLE_NAME, INDEX_NAME
ORDER BY TABLE_NAME, INDEX_NAME;
```

### 5. 菜单

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

## 六、不属于 SQL 的必要配置

以下配置不能通过 SQL 完成：

```yaml
WECHAT:
  app-id: ${WECHAT_APP_ID}
  app-secret: ${WECHAT_APP_SECRET}
  message-token: ${WECHAT_MESSAGE_TOKEN}
```

还需要在微信后台配置媒体审核回调：

```text
https://正式域名/api/blade-contentaudit/wechat/media-callback
```

同时保证动态图片和视频封面是微信服务器可以直接访问的公网 HTTP/HTTPS 地址。

## 七、重复执行说明

- `content-publish-workflow-migration.sql`：字段和普通索引可重复执行，但唯一索引要求历史数据无重复；
- `comment_audit_fields_compatibility_upgrade.sql`：字段和索引均按存在性判断，可重复执行；
- `content_audit_retry_upgrade.sql`：支持建表、旧表补列和重复执行；
- `dynamic_content_auto_audit_upgrade.sql`：按索引名判断，可重复执行；
- `contentmoderation.menu.sql`：使用 `ON DUPLICATE KEY UPDATE`，但必须先确认菜单 ID/code 没有被其他业务占用。

## 八、本次不需要新增的 SQL

本次不需要新增：

- 微信 AppID/AppSecret 配置表；
- 收费审核服务配置表；
- 视频逐帧审核表；
- OCR 审核表；
- 新的动态主表。

微信密钥和回调 Token 必须放在环境变量或密钥管理中，不能写入 SQL 或 Git 仓库。
