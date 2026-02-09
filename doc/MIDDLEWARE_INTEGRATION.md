# 社交模块中间件集成指南

## 概述

本文档说明社交模块(Social Module)的中间件集成情况,包括ElasticSearch、RabbitMQ、WebSocket的配置和使用。

## 1. ElasticSearch集成

### 1.1 配置说明

在`application-dev.yml`中配置:

```yaml
social:
  elasticsearch:
    enabled: true  # 启用ElasticSearch
    host: localhost
    port: 9200
    scheme: http
```

### 1.2 核心类

- **配置类**: `org.springblade.modules.social.config.ElasticSearchConfig`
- **服务接口**: `org.springblade.modules.social.service.ElasticSearchService`
- **服务实现**: `org.springblade.modules.social.service.impl.ElasticSearchServiceImpl`

### 1.3 功能说明

- **索引名称**: `social_img_detail`
- **主要功能**:
  - 添加/更新/删除图片文档
  - 批量添加文档
  - 全文搜索(支持标题、内容、标签)
  - 分页查询(支持最新/最热排序)
  - 搜索记录管理

### 1.4 使用示例

```java
@Autowired
private ElasticSearchService elasticSearchService;

// 添加文档
elasticSearchService.addDocument(imgDetailVo);

// 搜索
Map<String, Object> result = elasticSearchService.search("关键词", 1, 10);

// 删除文档
elasticSearchService.deleteDocument("123");
```

## 2. RabbitMQ集成

### 2.1 配置说明

在`application-dev.yml`中配置:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        prefetch: 1
        concurrency: 3
        acknowledge-mode: manual
```

### 2.2 核心类

- **配置类**: `org.springblade.modules.social.config.RabbitMQConfig`
- **工具类**: `org.springblade.modules.social.utils.MessageQueueUtil`

### 2.3 队列说明

| 队列名称 | 路由键 | 用途 |
|---------|--------|------|
| social.agree.queue | social.agree | 点赞消息 |
| social.comment.queue | social.comment | 评论消息 |
| social.follow.queue | social.follow | 关注消息 |
| social.img.queue | social.img | 图片消息 |

### 2.4 使用示例

```java
@Autowired
private MessageQueueUtil messageQueueUtil;

// 发送消息
messageQueueUtil.sendMessage(
    RabbitMQConfig.SOCIAL_EXCHANGE,
    RabbitMQConfig.AGREE_ROUTING_KEY,
    agreeMessage
);

// 发送延迟消息
messageQueueUtil.sendDelayMessage(
    RabbitMQConfig.SOCIAL_EXCHANGE,
    RabbitMQConfig.COMMENT_ROUTING_KEY,
    commentMessage,
    5000  // 延迟5秒
);
```

## 3. WebSocket集成

### 3.1 配置说明

在`application-dev.yml`中配置:

```yaml
social:
  websocket:
    enabled: true  # 启用WebSocket
```

### 3.2 核心类

- **配置类**: `org.springblade.modules.social.config.WebSocketConfig`
- **处理器**: `org.springblade.modules.social.websocket.SocialWebSocketHandler`

### 3.3 端点说明

- **STOMP端点**: `/ws/social`
- **消息前缀**: `/app`
- **用户前缀**: `/user`
- **订阅前缀**: `/topic`, `/queue`

### 3.4 通知类型

| 通知类型 | 订阅地址 | 说明 |
|---------|---------|------|
| 点赞通知 | /user/queue/agree | 用户收到点赞 |
| 评论通知 | /user/queue/comment | 用户收到评论 |
| 关注通知 | /user/queue/follow | 用户被关注 |
| 广播消息 | /topic/social | 全局广播 |

### 3.5 使用示例

**后端发送通知**:
```java
@Autowired
private SocialWebSocketHandler webSocketHandler;

// 发送点赞通知
Map<String, Object> message = Map.of(
    "type", "agree",
    "userId", "123",
    "content", "有人点赞了你的图片"
);
webSocketHandler.sendAgreeNotification("123", message);
```

**前端连接示例**(JavaScript):
```javascript
// 连接WebSocket
const socket = new SockJS('/ws/social');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 订阅点赞通知
    stompClient.subscribe('/user/queue/agree', function(message) {
        console.log('收到点赞通知:', JSON.parse(message.body));
    });
    
    // 订阅评论通知
    stompClient.subscribe('/user/queue/comment', function(message) {
        console.log('收到评论通知:', JSON.parse(message.body));
    });
});
```

## 4. 集成状态

### 4.1 当前状态

- ✅ ElasticSearch配置类已创建
- ✅ RabbitMQ配置类已创建
- ✅ WebSocket配置类已创建
- ✅ 工具类已创建
- ⏳ Service层TODO标记已添加
- ⏳ 需要根据实际需求启用中间件

### 4.2 启用步骤

1. **启用ElasticSearch**:
   - 确保ElasticSearch服务运行在localhost:9200
   - 修改`application-dev.yml`: `social.elasticsearch.enabled: true`
   - 重启应用

2. **启用RabbitMQ**:
   - 确保RabbitMQ服务运行在localhost:5672
   - 配置已自动生效(有配置即启用)
   - 重启应用

3. **启用WebSocket**:
   - 修改`application-dev.yml`: `social.websocket.enabled: true`
   - 重启应用

## 5. 待完成工作

### 5.1 Service层集成

需要在以下Service实现类中完成TODO标记的功能:

- **ImgDetailServiceImpl**:
  - 发布图片时同步到ElasticSearch
  - 更新图片时同步到ElasticSearch
  - 删除图片时从ElasticSearch删除

- **AgreeCollectServiceImpl**:
  - 点赞时通过MQ异步更新计数
  - 点赞时通过WebSocket通知用户

- **CommentServiceImpl**:
  - 评论时通过MQ异步更新计数
  - 评论时通过WebSocket通知用户

- **FollowServiceImpl**:
  - 关注时通过WebSocket通知用户

### 5.2 消息消费者

需要创建RabbitMQ消息消费者来处理队列中的消息。

## 6. 注意事项

1. **条件装配**: 所有中间件相关的Bean都使用了`@ConditionalOnProperty`或`@ConditionalOnBean`,只有在配置启用或依赖存在时才会加载
2. **优雅降级**: 如果中间件未启用,相关功能会被跳过,不影响核心业务
3. **日志记录**: 所有中间件操作都有详细的日志记录,便于调试和监控
4. **异常处理**: 中间件操作失败不会影响主业务流程

## 7. 性能优化建议

1. **ElasticSearch**:
   - 使用批量操作减少网络开销
   - 合理设置分片和副本数
   - 定期清理过期索引

2. **RabbitMQ**:
   - 使用消息确认机制保证可靠性
   - 合理设置预取数量(prefetch)
   - 使用死信队列处理失败消息

3. **WebSocket**:
   - 使用心跳机制保持连接
   - 合理设置连接超时时间
   - 使用消息压缩减少带宽

---

**更新时间**: 2026-01-26
**版本**: 1.0.0

