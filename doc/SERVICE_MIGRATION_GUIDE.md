# Service层迁移指南

## 📋 概述

本文档说明Service层迁移的详细步骤和注意事项。

## ✅ 已完成的工作

### 1. Service接口创建 (13个)
- [x] ImgDetailService
- [x] CategoryService
- [x] CommentService
- [x] AgreeCollectService
- [x] FollowService
- [x] AlbumService
- [x] TagService
- [x] SocialUserExtService
- [x] AlbumImgRelationService
- [x] TagImgRelationService
- [x] MessageService
- [x] MessageUserRelationService
- [x] UserOtherLoginRelationService

### 2. 简单Service实现类创建 (5个)
- [x] AlbumImgRelationServiceImpl
- [x] TagImgRelationServiceImpl
- [x] MessageServiceImpl
- [x] MessageUserRelationServiceImpl
- [x] UserOtherLoginRelationServiceImpl

## 📝 待完成的工作

### 1. 创建DTO类 (9个)

需要在`org.springblade.modules.social.pojo.dto`包下创建以下DTO类:

#### ImgDetailDTO
```java
- Long id
- Long categoryId
- Long categoryPid
- String content
- String cover
- Long userId
- Long albumId
- String imgsUrl
- Integer count
- Integer sort
- List<Tag> tags
- Integer status
- Integer type (0:新增, 1:修改)
```

#### CommentDTO
```java
- Long mid (图片ID)
- Long uid (用户ID)
- Long pid (父评论ID)
- Long replyId (回复评论ID)
- Long replyUid (回复用户ID)
- Integer level (评论等级)
- String content (评论内容)
```

#### AgreeCollectDTO
```java
- Long uid (当前用户ID)
- Long agreeCollectId (点赞/收藏对象ID)
- Long agreeCollectUid (被点赞用户ID)
- Integer type (0:点赞评论, 1:点赞图片, 2:收藏图片, 3:收藏专辑)
```

#### FollowDTO
```java
- Long uid (用户ID)
- Long fid (关注用户ID)
```

#### AlbumDTO
```java
- Long id
- String name
- Long uid
- String cover
- Integer sort
```

#### TagDTO
```java
- String name
- Integer sort
```

#### BrowseRecordDTO
```java
- Long uid (用户ID)
- Long mid (图片ID)
```

#### SearchRecordDTO
```java
- String uid (用户ID)
- String keyword (搜索关键词)
```

#### AlbumImgRelationDTO
```java
- Long aid (专辑ID)
- Long mid (图片ID)
```

### 2. 创建VO类 (8个)

需要在`org.springblade.modules.social.pojo.vo`包下创建以下VO类:

#### ImgDetailVo
```java
- Long id
- String content
- String cover
- Long userId
- Long categoryId
- String categoryName
- Long categoryPid
- String categoryPName
- String albumName
- Long imgCount
- Integer status
- String avatar
- String username
- List<String> imgsUrl
- Integer count
- List<Tag> tagList
- Long viewCount
- Long agreeCount
- Long collectionCount
- Long commentCount
- Date time
```

#### CategoryVo (继承TreeNode)
```java
- String name
- String description
- Long count
- String cover
- String hotCover
```

#### TagVo
```java
- Long count
- String name
```

#### AlbumVo
```java
- Long id
- String name
- String cover
- Integer sort
- Long imgCount
- Long collectionCount
- Long uid
- String username
- String avatar
```

#### FollowVo
```java
- Long uid
- String username
- String avatar
- boolean isfollow
- Long userId
- Long fanCount
- Date time
```

#### AgreeCollectVo
```java
- Long aid (专辑ID)
- Long mid (图片ID)
- String cover
- Long uid
- String username
- String avatar
- String content
- String name
- Integer count
- Long imgCount
- Long collectionCount
- Integer type (0:评论, 1:图片, 2:专辑)
- Date createDate
```

#### UserVo
```java
- Long id
- Long userId
- String username
- String avatar
- Integer gender
- String phone
- String email
- String description
- Integer status
- String birthday
- String address
- String cover
- Long trendCount
- Long followCount
- Long fanCount
```

#### UserRecordVo
```java
- Long uid
- Long addFollowCount (新关注数量)
- Long noreplyCount (未回复数量)
- Long agreeCollectionCount (新点赞收藏数量)
```

### 3. 创建复杂Service实现类 (8个)

以下Service实现类包含复杂业务逻辑,需要在创建DTO/VO后实现:

- [ ] ImgDetailServiceImpl
- [ ] CategoryServiceImpl
- [ ] CommentServiceImpl
- [ ] AgreeCollectServiceImpl
- [ ] FollowServiceImpl
- [ ] AlbumServiceImpl
- [ ] TagServiceImpl
- [ ] SocialUserExtServiceImpl

## 🔑 关键技术点

### 1. 依赖注入适配

**烟火项目**:
```java
@Autowired
RedisUtils redisUtils;

@Autowired
SendMessageMq sendMessageMq;
```

**BladeX项目** (需要适配):
- 使用BladeX的Redis工具类
- 使用BladeX的消息队列工具
- 使用BladeX的OSS工具类

### 2. 用户ID获取

**烟火项目**:
```java
// 从JWT token中获取
Long userId = JwtUtils.getUserId(token);
```

**BladeX项目**:
```java
// 使用BladeX的AuthUtil
Long userId = AuthUtil.getUserId();
```

### 3. 缓存注解

**烟火项目**:
```java
@Cacheable(cacheNames = PlatformConstant.CATEGORY, key = PlatformConstant.CATEGORY_KEY)
```

**BladeX项目** (需要适配):
- 使用BladeX的缓存配置
- 调整缓存Key命名规则

### 4. 事务管理

保持不变,继续使用:
```java
@Transactional(rollbackFor = Exception.class)
```

## 📌 下一步计划

1. 创建所有DTO类
2. 创建所有VO类
3. 创建复杂Service实现类
4. 适配BladeX的工具类和服务
5. 进行单元测试

## ⚠️ 注意事项

1. **字段映射**: 注意`username`改为`name`(blade_user表字段)
2. **日期字段**: `createDate`改为`createTime`, `updateDate`改为`updateTime`
3. **用户表关联**: 所有关联`t_user`的地方改为`blade_user`
4. **Swagger注解**: 使用`@Schema`替代`@ApiModel`和`@ApiModelProperty`
5. **验证注解**: 保持使用`javax.validation`的注解


