# 推荐系统迁移文档

## 概述

本文档说明社交模块推荐系统的实现原理、配置方法和使用说明。

## 1. 推荐系统架构

### 1.1 技术栈

- **DJL (Deep Java Library)**: AI框架,用于加载和运行深度学习模型
- **PyTorch**: 深度学习引擎
- **Sentence Transformers**: 句子嵌入模型(distiluse-base-multilingual-cased-v1)
- **TextRank**: 关键词提取算法
- **Jcseg**: 中文分词工具

### 1.2 核心组件

| 组件 | 说明 | 位置 |
|------|------|------|
| FeatureComparison | 特征比较工具(余弦相似度) | recommend/FeatureComparison.java |
| KeywordsExtractor | 关键词提取工具 | recommend/KeywordsExtractor.java |
| SentenceEncoder | 句子编码器配置 | recommend/SentenceEncoder.java |
| SentenceTransTranslator | DJL翻译器 | recommend/SentenceTransTranslator.java |
| RecommendUtils | 推荐工具类 | recommend/RecommendUtils.java |
| RecommendService | 推荐服务接口 | service/RecommendService.java |
| RecommendServiceImpl | 推荐服务实现 | service/impl/RecommendServiceImpl.java |
| RecommendController | 推荐控制器 | controller/RecommendController.java |

## 2. 推荐算法

### 2.1 协同过滤推荐(CF)

**算法流程**:
1. 获取用户浏览历史(前6条)
2. 从图片库中随机选择一批图片
3. 过滤掉已浏览的图片
4. 返回推荐结果

**特点**:
- 简单快速
- 不需要模型
- 适合冷启动场景

### 2.2 机器学习推荐(ML)

**算法流程**:
1. 获取用户浏览历史(前3条)
2. 提取图片内容特征(标题+标签+分类)
3. 使用TextRank提取关键词
4. 使用Sentence Transformer生成句子嵌入向量
5. 计算候选图片与浏览历史的余弦相似度
6. 按相似度排序,返回Top-N推荐

**特点**:
- 基于内容的推荐
- 个性化程度高
- 需要GPU/CPU计算资源

## 3. 配置说明

### 3.1 启用推荐系统

在`application-dev.yml`中配置:

```yaml
social:
  recommend:
    enabled: true  # 启用推荐系统
    model-path: /root/jar/distiluse-base-multilingual-cased-v1.zip  # 模型路径
```

### 3.2 模型下载

**模型**: distiluse-base-multilingual-cased-v1

**下载地址**:
- HuggingFace: https://huggingface.co/sentence-transformers/distiluse-base-multilingual-cased-v1
- 百度网盘: (需要自行上传)

**模型说明**:
- 支持50+种语言
- 向量维度: 512
- 模型大小: ~500MB

### 3.3 依赖配置

在`pom.xml`中添加:

```xml
<!-- DJL (Deep Java Library) -->
<dependency>
    <groupId>ai.djl</groupId>
    <artifactId>api</artifactId>
    <version>0.20.0</version>
</dependency>
<dependency>
    <groupId>ai.djl.pytorch</groupId>
    <artifactId>pytorch-engine</artifactId>
    <version>0.20.0</version>
</dependency>
<dependency>
    <groupId>ai.djl.pytorch</groupId>
    <artifactId>pytorch-model-zoo</artifactId>
    <version>0.20.0</version>
</dependency>

<!-- Jcseg中文分词 -->
<dependency>
    <groupId>org.lionsoul</groupId>
    <artifactId>jcseg-core</artifactId>
    <version>2.6.2</version>
</dependency>

<!-- Google Guava -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>31.1-jre</version>
</dependency>
```

## 4. API接口

### 4.1 协同过滤推荐

**接口**: `GET /api/social/recommend/cf/{page}/{limit}`

**参数**:
- page: 页码
- limit: 每页数量
- uid: 用户ID

**响应**:
```json
{
  "code": 200,
  "success": true,
  "data": {
    "records": [...],
    "total": 24
  }
}
```

### 4.2 机器学习推荐

**接口**: `GET /api/social/recommend/ml/{page}/{limit}`

**参数**:
- page: 页码
- limit: 每页数量
- uid: 用户ID

**响应**:
```json
{
  "code": 200,
  "success": true,
  "data": {
    "records": [...],
    "total": 10
  }
}
```

## 5. 性能优化

### 5.1 向量缓存

- 使用Redis缓存图片的嵌入向量
- Key格式: `social:recommend:{imgId}`
- 避免重复计算,提升性能

### 5.2 批量处理

- 使用Google Guava的Lists.partition进行分批处理
- 减少内存占用

### 5.3 异步计算

- 可以使用异步任务预计算热门图片的向量
- 定时更新向量缓存

## 6. 注意事项

1. **模型路径**: 确保模型文件存在且路径正确
2. **内存要求**: 模型加载需要约1GB内存
3. **CPU/GPU**: 推荐使用GPU加速,CPU也可运行但较慢
4. **条件装配**: 推荐系统使用条件装配,未启用时不影响其他功能
5. **浏览历史**: 需要先有用户浏览记录才能进行推荐

## 7. 故障排查

### 7.1 模型加载失败

**问题**: ModelNotFoundException

**解决**:
- 检查模型路径是否正确
- 检查模型文件是否完整
- 检查文件权限

### 7.2 内存不足

**问题**: OutOfMemoryError

**解决**:
- 增加JVM堆内存: `-Xmx2g`
- 减少并发请求数
- 使用模型量化版本

### 7.3 推荐结果为空

**问题**: 返回空列表

**解决**:
- 检查用户是否有浏览历史
- 检查Redis中是否有图片数据
- 检查图片内容是否为空

---

**更新时间**: 2026-01-26
**版本**: 1.0.0

