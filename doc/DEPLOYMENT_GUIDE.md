# 社交模块部署指南

## 概述

本文档说明如何部署和配置BladeX社交模块,包括环境准备、依赖安装、配置说明和启动步骤。

## 1. 环境要求

### 1.1 基础环境

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 必须 |
| Maven | 3.6+ | 必须 |
| MySQL | 5.7+ | 必须 |
| Redis | 5.0+ | 必须 |
| ElasticSearch | 8.x | 可选 |
| RabbitMQ | 3.8+ | 可选 |

### 1.2 中间件环境(可选)

| 中间件 | 用途 | 是否必须 |
|--------|------|---------|
| ElasticSearch | 全文搜索 | 否 |
| RabbitMQ | 消息队列 | 否 |
| DJL模型 | AI推荐 | 否 |

## 2. 数据库初始化

### 2.1 执行SQL脚本

```bash
# 进入SQL目录
cd doc/sql

# 执行社交模块迁移脚本
mysql -u root -p ldqc < social_migration.sql
```

### 2.2 验证表创建

```sql
-- 查看社交模块表
SHOW TABLES LIKE 't_%';
SHOW TABLES LIKE 'social_%';

-- 应该看到以下表:
-- social_user_ext
-- t_img_detail
-- t_category
-- t_comment
-- t_agree_collect
-- t_follow
-- t_album
-- t_album_img_relation
-- t_tag
-- t_tag_img_relation
-- t_message
-- t_message_user_relation
-- t_user_other_login_relation
```

## 3. 依赖配置

### 3.1 Maven依赖

所有依赖已在`pom.xml`中配置,执行以下命令安装:

```bash
mvn clean install -DskipTests
```

### 3.2 关键依赖说明

```xml
<!-- ElasticSearch -->
<dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
    <version>8.11.0</version>
</dependency>

<!-- RabbitMQ -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- DJL AI推荐 -->
<dependency>
    <groupId>ai.djl</groupId>
    <artifactId>api</artifactId>
    <version>0.20.0</version>
</dependency>

<!-- Jcseg中文分词 -->
<dependency>
    <groupId>org.lionsoul</groupId>
    <artifactId>jcseg-core</artifactId>
    <version>2.6.3</version>
</dependency>

<!-- Google Guava -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>31.1-jre</version>
</dependency>
```

## 4. 配置文件

### 4.1 基础配置(application-dev.yml)

```yaml
# 数据源配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ldqc?useSSL=false&useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password
  
  # Redis配置
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password:
      database: 8

# OSS配置(图片存储)
oss:
  enabled: true
  name: minio  # 或 qiniu、alioss等
  endpoint: http://127.0.0.1:9000
  access-key: your_access_key
  secret-key: your_secret_key
  bucket-name: social
```

### 4.2 社交模块配置

```yaml
# 社交模块配置
social:
  # ElasticSearch配置(可选)
  elasticsearch:
    enabled: false  # 启用设置为true
    host: localhost
    port: 9200
    scheme: http
  
  # WebSocket配置(可选)
  websocket:
    enabled: false  # 启用设置为true
  
  # 推荐系统配置(可选)
  recommend:
    enabled: false  # 启用设置为true
    model-path: /path/to/distiluse-base-multilingual-cased-v1.zip

# RabbitMQ配置(可选)
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

## 5. 中间件安装(可选)

### 5.1 ElasticSearch安装

**Docker方式**:
```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:8.11.0
```

**验证**:
```bash
curl http://localhost:9200
```

### 5.2 RabbitMQ安装

**Docker方式**:
```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.12-management
```

**访问管理界面**:
- URL: http://localhost:15672
- 用户名: guest
- 密码: guest

### 5.3 推荐模型下载

**模型**: distiluse-base-multilingual-cased-v1

**下载地址**:
```bash
# HuggingFace
wget https://huggingface.co/sentence-transformers/distiluse-base-multilingual-cased-v1/resolve/main/pytorch_model.bin

# 或使用git lfs
git lfs install
git clone https://huggingface.co/sentence-transformers/distiluse-base-multilingual-cased-v1
```

**配置路径**:
```yaml
social:
  recommend:
    model-path: /path/to/distiluse-base-multilingual-cased-v1.zip
```

## 6. 启动应用

### 6.1 开发环境启动

```bash
# 方式1: Maven启动
mvn spring-boot:run

# 方式2: IDE启动
# 运行 org.springblade.core.launch.BladeApplication
```

### 6.2 生产环境启动

```bash
# 打包
mvn clean package -DskipTests

# 启动
java -jar target/blade-api.jar --spring.profiles.active=prod
```

### 6.3 Docker启动

```bash
# 构建镜像
docker build -t bladex-social:latest .

# 运行容器
docker run -d \
  --name bladex-social \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  bladex-social:latest
```

## 7. 验证部署

### 7.1 健康检查

```bash
# 检查应用状态
curl http://localhost:8080/actuator/health

# 预期响应
{
  "status": "UP"
}
```

### 7.2 API文档访问

访问Swagger文档:
- URL: http://localhost:8080/doc.html
- 查找"社交模块"相关接口

### 7.3 测试接口

```bash
# 测试分类接口
curl http://localhost:8080/api/social/category/tree

# 测试标签接口
curl http://localhost:8080/api/social/tag/list
```

## 8. 故障排查

### 8.1 常见问题

**问题1**: 数据库连接失败
```
解决: 检查数据库配置、网络连接、用户权限
```

**问题2**: Redis连接失败
```
解决: 检查Redis服务状态、端口、密码配置
```

**问题3**: ElasticSearch连接失败
```
解决: 
1. 检查ES服务状态
2. 确认配置中enabled=true
3. 检查端口和scheme配置
```

**问题4**: 推荐系统模型加载失败
```
解决:
1. 检查模型文件路径
2. 确认模型文件完整性
3. 检查文件权限
4. 增加JVM内存: -Xmx2g
```

### 8.2 日志查看

```bash
# 查看应用日志
tail -f logs/blade.log

# 查看错误日志
tail -f logs/blade-error.log
```

## 9. 性能优化

### 9.1 JVM参数

```bash
java -jar blade-api.jar \
  -Xms2g \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

### 9.2 数据库优化

```sql
-- 添加索引
ALTER TABLE t_img_detail ADD INDEX idx_user_id (user_id);
ALTER TABLE t_img_detail ADD INDEX idx_category_id (category_id);
ALTER TABLE t_comment ADD INDEX idx_mid (mid);
ALTER TABLE t_agree_collect ADD INDEX idx_mid_type (mid, type);
```

### 9.3 Redis优化

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
```

## 10. 监控和维护

### 10.1 监控指标

- 应用健康状态
- 数据库连接池
- Redis连接状态
- 中间件连接状态
- API响应时间

### 10.2 定期维护

- 清理过期缓存
- 备份数据库
- 更新ElasticSearch索引
- 清理RabbitMQ死信队列

---

**更新时间**: 2026-01-26
**版本**: 1.0.0

