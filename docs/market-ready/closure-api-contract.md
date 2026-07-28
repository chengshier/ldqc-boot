# 产品收口接口与运营契约

更新日期：2026-07-28

本文只描述本轮新增或收口的关键接口，完整字段以 OpenAPI 和源码 DTO/VO 为准。

## 一、评论内容安全审核

### 1. 状态约定

| 状态值 | 状态 | 用户可见 | 运营动作 |
|---|---|---|---|
| `0` | 处理中 | 否 | 无 |
| `1` | 审核通过 | 是 | 可按普通评论管理 |
| `2` | 审核拒绝 | 否 | 查看拒绝原因 |
| `3` | 自动重试中 | 否 | 可立即重试 |
| `4` | 待人工处理 | 否 | 人工通过、人工拒绝或再次重试 |

### 2. 自动重试规则

- 初次调用失败时创建审核任务；
- 默认一分钟后开始第一次自动重试；
- 退避间隔：1、5、15、60、180 分钟；
- 最多自动尝试五次；
- 达到上限后转为状态 `4`，不再自动公开评论；
- 运营人员人工通过后才更新评论数量；
- 人工拒绝或审核服务拒绝后向用户发送站内消息。

可通过配置调整调度频率：

```yaml
content-audit:
  retry:
    initial-delay-ms: 30000
    fixed-delay-ms: 60000
```

### 3. 小程序接口

#### 社区评论

```text
POST /blade-comment/comment/addComment
```

客户端只需要提交：

```json
{
  "mid": 10001,
  "pid": 0,
  "content": "评论内容"
}
```

`uid` 即使被客户端传入也不能作为身份依据，正式身份由后端 Token 决定。

#### 新闻评论

```text
POST /blade-newscomment/newsComment/mobile/save
```

```json
{
  "newsId": 10001,
  "parentId": 0,
  "content": "评论内容"
}
```

用户 ID、昵称和头像全部由服务端读取。

### 4. 管理端异常待办接口

```text
GET  /blade-contentaudit/task/page
GET  /blade-contentaudit/task/summary
POST /blade-contentaudit/task/retry-now
POST /blade-contentaudit/task/resolve
```

分页支持：

- `bizType=TREND_COMMENT|NEWS_COMMENT`
- `auditStatus=3|4`
- 标准 `current`、`size`

立即重试：

```json
{
  "taskId": 10001
}
```

人工处理：

```json
{
  "taskId": 10001,
  "action": "PASS",
  "reason": "运营人员核对后确认正常"
}
```

`action` 只允许 `PASS` 或 `REJECT`；人工拒绝应填写明确原因。

### 5. SQL

```text
doc/sql/content_audit_retry_upgrade.sql
src/main/java/sql/contentmoderation.menu.sql
```

测试库必须验证：

- 状态 3 自动进入调度；
- 五次失败转状态 4；
- 同一任务不会被并发重复处理；
- 人工通过只增加一次评论数；
- 人工拒绝会产生站内消息。

## 二、绿动有约运营工作台

### 1. 用户业务接口

```text
GET  /blade-sportinvite/sportInvite/page
GET  /blade-sportinvite/sportInvite/app-detail
POST /blade-sportinvite/sportInvite/submit
POST /blade-sportinvite/sportInvite/cancel
POST /blade-sportinvite/sportInvite/apply
GET  /blade-sportinvite/sportInvite/myPublish
GET  /blade-sportinvite/sportInvite/myApply
GET  /blade-sportinvite/sportInvite/applyList
POST /blade-sportinvite/sportInvite/audit
```

核心规则：

- 发布人和申请人以后端登录态为准；
- 申请通过使用条件更新原子占用名额；
- 只有发布人能审核自己的申请；
- 只有发布人和已通过参与者能查看完整联系方式；
- 重复审核、满员审核和活动开始后的申请会被拒绝。

### 2. 管理端运营接口

```text
GET  /blade-sportinvite/sportInvite/admin/summary
GET  /blade-sportinvite/sportInvite/admin/applications
POST /blade-sportinvite/sportInvite/admin/audit
```

汇总返回：

```json
{
  "inviteTotal": 120,
  "openInviteCount": 25,
  "fullInviteCount": 8,
  "pendingApplyCount": 16
}
```

申请分页支持：

- `inviteId`
- `applyStatus=PENDING|APPROVED|REJECTED|CANCELED`
- `current`、`size`

平台代审：

```json
{
  "applyId": 10001,
  "auditAction": "APPROVE",
  "auditRemark": "发起人长期未处理，平台核对后代审"
}
```

管理员代审仍复用同一套名额状态机，并写入带“平台代处理”标记的审核日志。

## 三、推荐行为反馈

### 1. 事件类型

```text
IMPRESSION       曝光
CLICK            点击
DWELL            有效停留
VIDEO_COMPLETE   视频完整播放
NOT_INTERESTED   不感兴趣
```

支持内容类型：

```text
CONTENT
NEWS
```

### 2. 上报接口

```text
POST /blade-recommend-feedback/feedback/record
```

示例：

```json
{
  "requestId": "DWELL_CONTENT_10001_1722150000000",
  "sessionId": "CONTENT_10001_1722150000000",
  "contentType": "CONTENT",
  "contentId": 10001,
  "eventType": "DWELL",
  "durationMs": 18000
}
```

身份规则：

- 用户 ID 只从后端登录态获取；
- 同一用户相同 `requestId` 幂等；
- 非法事件类型和内容类型直接拒绝；
- 时长解析失败按 0 处理，不允许负值。

### 3. 排序规则

当前推荐排序综合：

- 用户主动选择的兴趣分类；
- 最近浏览内容分类；
- 点击行为；
- 有效停留时长；
- 视频完整播放；
- 内容发布时间；
- 不感兴趣过滤。

`NOT_INTERESTED` 会立即从该用户后续内容推荐中排除对应内容。行为权重只作为排序信号，不改变内容审核状态。

## 四、场馆预约延期边界

本轮没有在线订场接口。当前场馆范围为：

- 入驻申请；
- 平台审核；
- 场馆资料展示；
- 场馆运营者维护；
- 优惠券范围和核销。

场地、时段、价格、支付、取消、退款、到店核销和结算进入后续独立阶段。详见：

```text
docs/market-ready/venue-booking-deferred-boundary.md
```

## 五、联调顺序

1. 执行结构迁移 SQL；
2. 执行菜单和按钮权限 SQL；
3. 启动后端并确认定时任务无异常；
4. 使用运营管理员验证异常待办和绿动有约工作台；
5. 使用两个普通用户验证评论、邀约申请和联系方式权限；
6. 使用推荐页产生曝光、点击、不感兴趣、停留和播放完成记录；
7. 核对数据库状态、数量、消息和审核日志；
8. 最后进入微信开发者工具和真机回归。
