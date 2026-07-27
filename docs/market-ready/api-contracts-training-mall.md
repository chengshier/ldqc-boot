# 长视频课程与积分商城接口契约

> 本文对应 `refactor/market-ready-phase1` 分支。未经测试库、编译和真机验证的内容仍需按验收清单执行。

## 一、SQL 执行顺序

### 长视频课程

1. `doc/sql/training-video-course-migration.sql`
2. `src/main/java/sql/trainingcourseworkbench.menu.sql`

### 积分商城

1. `doc/sql/mall-exchange-fulfillment-migration.sql`
2. `doc/sql/mall-exchange-fulfillment-data-fix.sql`
3. `src/main/java/sql/malloperation.menu.sql`

执行前必须备份：

- `ldqc_training`
- `mall_product`
- `mall_exchange_order`
- `points_account`
- `points_ledger`

---

# 二、长视频课程

## 2.1 课程数据边界

- `ldqc_training`：课程主表，保存名称、封面、价格、课程形态和发布状态。
- `ldqc_training_chapter`：章节。
- `ldqc_training_lesson`：课时和视频媒体状态。
- `ldqc_training_access`：用户播放授权。
- `ldqc_training_progress`：用户断点续播进度。

课程形态：

- `OFFLINE`：线下课程。
- `ONLINE`：线上长视频课程。
- `MIXED`：线上课程与线下服务并存。

发布状态：

- `DRAFT`：草稿。
- `PENDING`：待审核，预留。
- `PUBLISHED`：已发布。
- `REJECTED`：审核驳回，预留达人课程审核。
- `OFFLINE`：已下架。

媒体状态：

- `UPLOADING`：上传中。
- `PROCESSING`：封面、时长或转码处理中。
- `READY`：可播放。
- `FAILED`：处理失败。

## 2.2 小程序课程接口

### 课程列表

```http
GET /blade-training/training/mobile-page
```

查询参数：

- `current`
- `size`
- `category`，可选
- `courseType`，可选
- `contentMode`，可选

只返回 `status=1` 且 `publish_status=PUBLISHED` 的课程。

### 课程详情与目录

```http
GET /blade-training/training/mobile-detail?id={trainingId}
```

返回：

- 课程基础资料；
- `authorized`；
- `purchaseRequired`；
- 章节和课时；
- 课时 `trial/playable/locked`；
- 学习进度。

**目录接口不返回 `videoUrl`。**

### 获取播放令牌

```http
POST /blade-training/training/lesson-play-token
Content-Type: application/json

{
  "lessonId": 10001
}
```

允许条件：

- 免费课程；
- 试看课时；
- 用户存在有效 `ldqc_training_access`。

返回五分钟短时播放地址。

### 视频播放跳转

```http
GET /blade-training/training/video-play?token={playToken}
```

播放器访问该地址，后端校验短时令牌后跳转实际视频资源。

说明：这属于访问控制，不是 DRM。对象存储最终地址仍可能被终端播放器看到。

### 保存学习进度

```http
POST /blade-training/training/progress
Content-Type: application/json

{
  "lessonId": 10001,
  "progressSeconds": 620
}
```

建议：

- 每 20～30 秒上报；
- 页面隐藏、退出和播放结束时上报；
- 后端不允许较旧心跳覆盖更靠后的断点；
- 播放达到 90% 或距离结束 10 秒以内视为完成。

## 2.3 管理端课程工作台接口

基础路径：

```text
/blade-training/course-admin
```

主要接口：

- `GET /outline?trainingId=`：课程章节课时总览。
- `POST /settings`：新建或修改课程基础资料。
- `POST /chapter/save`：保存章节。
- `POST /chapter/delete`：删除空章节。
- `POST /lesson/save`：保存课时；视频变化时进入媒体处理。
- `POST /lesson/reprocess`：重新处理视频。
- `POST /lesson/delete`：删除课时。
- `POST /publish`：检查课时与媒体状态后发布。
- `POST /offline`：填写原因后下架。
- `GET /user-options?keyword=`：搜索播放授权用户。
- `GET /access/list?trainingId=`：授权列表。
- `POST /access/grant`：人工、活动或线下订单授权。
- `POST /access/revoke`：撤销授权。

管理端页面：

- `src/views/training/courseManager.vue`
- `src/views/training/courseWorkbench.vue`

当前购买订单自动授权尚未接入。正式在线付费完成后，应由支付成功事件写入 `ldqc_training_access`，不依赖运营人工授权。

---

# 三、积分商城

## 3.1 业务边界

商城只支持：

- `SHIP`：快递到家；
- `PICKUP`：到店领取；
- `VIRTUAL`：虚拟权益。

优惠券不能作为商城商品维护。优惠券继续使用独立 Tab、券模板、领取与核销系统。

## 3.2 用户端商城接口

基础路径：

```text
/blade-mall/exchange
```

### 商品列表

```http
GET /product-page
```

支持：

- `keyword`
- `categoryCode`
- `fulfillmentType`
- `minPoints`
- `maxPoints`
- `current/size`

仅返回已上架、有库存且非优惠券商品。

### 商品详情

```http
GET /product-detail?id={productId}
```

返回：

- 主图和图集；
- 规格；
- 兑换绿豆；
- 库存；
- 限兑规则；
- 履约方式；
- 商家和领取地址；
- 兑换说明。

### 兑换确认

```http
GET /confirm?productId={id}&qty=1&spec={规格}
```

登录后调用，返回：

- 当前绿豆；
- 所需绿豆；
- 兑换后余额；
- 是否足够；
- 已兑换数量；
- 剩余限兑数量；
- 是否需要收货地址。

### 提交兑换

```http
POST /submit
Content-Type: application/json

{
  "productId": 10001,
  "qty": 1,
  "spec": "红色/M码",
  "requestId": "mall-1720000000000-abcd1234",
  "receiverName": "张三",
  "receiverPhone": "13800000000",
  "receiverAddress": "完整收货地址"
}
```

后端同一事务执行：

1. 请求号幂等；
2. 商品、规格、库存、单次和累计限兑校验；
3. 条件更新扣减库存；
4. 条件更新扣减绿豆；
5. 创建商品快照订单；
6. 创建绿豆流水。

前端不得传入或决定商品绿豆价格。

### 我的兑换

```http
GET /my-orders
GET /my-order-detail?id={orderId}
POST /confirm-receipt
```

用户只能查看自己的订单。

订单展示使用快照字段，不受商品后续改名、换图或下架影响。

## 3.3 履约状态

- `PENDING`：待处理。
- `PROCESSING`：处理中。
- `SENT`：快递已发货。
- `READY`：到店待领取或虚拟权益已发放。
- `COMPLETED`：已完成。
- `CANCELLED`：已取消并退款。

## 3.4 管理端履约接口

- `GET /admin-page`：运营订单列表。
- `POST /admin-ship`：填写物流并发货。
- `POST /admin-ready-pickup`：设置待领取。
- `POST /admin-issue-virtual`：发放虚拟权益。
- `POST /admin-complete`：核验领取码或完成虚拟订单。
- `POST /admin-cancel`：取消未发货订单，恢复库存并退还绿豆。

管理端页面：

- `src/views/mall/productManager.vue`
- `src/views/mall/fulfillmentWorkbench.vue`

## 3.5 商品运营接口

基础路径：

```text
/blade-mall/product-admin
```

- `GET /page`
- `GET /detail`
- `POST /save`
- `POST /status`

上架前强制检查：

- 商品名称；
- 商品主图；
- 兑换绿豆；
- 可用库存；
- 到店领取地址。

---

# 四、必须补验的场景

## 长视频

- 30、60、120 分钟视频；
- 弱网、切后台、锁屏、退出重进；
- 免费、试看、未购买、已授权、撤销授权；
- 播放令牌过期；
- 视频处理中和处理失败；
- Range 请求与对象存储响应头。

## 商城

- 同一请求重复提交；
- 不同请求并发兑换最后一件库存；
- 绿豆不足；
- 单次限兑和累计限兑；
- 快递、到店和虚拟权益三类订单；
- 发货后禁止直接取消；
- 取消退款库存、账户和流水一致；
- 历史商品改名、下架后订单快照不变。
