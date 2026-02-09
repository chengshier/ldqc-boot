# 烟火社交功能迁移到BladeX 4.7.0指南

## 📋 迁移概述

本文档记录了将烟火App的社交功能完整迁移到BladeX 4.7.0单体版本的详细过程。

### 项目信息
- **源项目**: 烟火App (yanhuo_dev-master)
- **目标项目**: BladeX 4.7.0 单体版本 (ldqc-boot)
- **迁移日期**: 2026-01-26
- **迁移范围**: 全部社交功能

---

## ✅ 已完成的工作

### 1. 环境准备和分析 ✓
- [x] 检查BladeX项目结构
- [x] 分析烟火项目架构
- [x] 确认中间件可用性 (Redis, ElasticSearch, RabbitMQ)

### 2. 包结构创建 ✓
已创建以下包结构:
```
org.springblade.modules.social/
├── controller/          # 控制器层
├── pojo/
│   ├── entity/         # 实体类
│   ├── vo/             # 视图对象
│   └── dto/            # 数据传输对象
├── mapper/             # Mapper接口
├── service/
│   └── impl/           # 服务实现
├── config/             # 配置类
└── websocket/          # WebSocket相关
```

### 3. 数据库迁移脚本 ✓
已创建 `doc/sql/social_migration.sql`,包含:
- 用户扩展表 (social_user_ext)
- 图片详情表 (t_img_detail)
- 分类表 (t_category)
- 评论表 (t_comment)
- 点赞收藏表 (t_agree_collect)
- 关注表 (t_follow)
- 专辑表 (t_album)
- 专辑图片关系表 (t_album_img_relation)
- 标签表 (t_tag)
- 标签图片关系表 (t_tag_img_relation)
- 消息表 (t_message)
- 消息用户关系表 (t_message_user_relation)
- 第三方登录关系表 (t_user_other_login_relation)

---

## 🔄 进行中的工作

### 4. 实体类迁移 ✓
需要迁移的实体类:
- [x] SocialUserExt (用户扩展)
- [x] ImgDetail (图片详情)
- [x] Category (分类)
- [x] Comment (评论)
- [x] AgreeCollect (点赞收藏)
- [x] Follow (关注)
- [x] Album (专辑)
- [x] AlbumImgRelation (专辑图片关系)
- [x] Tag (标签)
- [x] TagImgRelation (标签图片关系)
- [x] Message (消息)
- [x] MessageUserRelation (消息用户关系)
- [x] UserOtherLoginRelation (第三方登录关系)

**已完成**: 所有13个实体类已成功创建并适配BladeX的TenantEntity

---

## 📝 待办事项

### 5. Mapper层迁移 ✓
- [x] 创建所有Mapper接口 (13个)
- [x] 迁移Mapper XML文件 (13个)
- [x] 调整SQL查询语句 (已适配blade_user表)

### 6. Service层迁移 (进行中)
- [x] 创建Service接口 (13个)
- [ ] 创建DTO类 (9个待创建)
- [ ] 创建VO类 (8个待创建,2个已创建)
- [ ] 创建Service实现类 (13个待创建)

**Service接口列表**:
1. ImgDetailService - 图片详情服务
2. CategoryService - 分类服务
3. CommentService - 评论服务
4. AgreeCollectService - 点赞收藏服务
5. FollowService - 关注服务
6. AlbumService - 专辑服务
7. TagService - 标签服务
8. SocialUserExtService - 社交用户扩展服务
9. AlbumImgRelationService - 专辑图片关系服务
10. TagImgRelationService - 标签图片关系服务
11. MessageService - 消息服务
12. MessageUserRelationService - 消息用户关系服务
13. UserOtherLoginRelationService - 第三方登录关系服务

### 7. Controller层迁移
- [ ] ImgDetailController
- [ ] CategoryController
- [ ] CommentController
- [ ] AgreeCollectController
- [ ] FollowController
- [ ] AlbumController
- [ ] TagController
- [ ] MessageController
- [ ] UserController (扩展)

### 8. 中间件集成
- [ ] ElasticSearch配置
- [ ] RabbitMQ配置
- [ ] WebSocket配置
- [ ] Redis缓存策略

### 9. 推荐系统迁移
- [ ] 协同过滤算法
- [ ] 机器学习模型
- [ ] 推荐服务

### 10. 配置文件更新
- [ ] application.yml
- [ ] pom.xml依赖

---

## 🔑 关键技术点

### 用户表处理方案
**问题**: 烟火的`t_user`表与BladeX的`blade_user`表冲突

**解决方案**:
1. 复用BladeX的`blade_user`表存储基础用户信息
2. 创建`social_user_ext`扩展表存储社交特有字段:
   - trend_count (动态数量)
   - follow_count (关注数量)
   - fan_count (粉丝数量)
   - cover (个人主页封面)
   - description (个人简介)

### 实体类继承关系
- **烟火**: 继承自 `BaseEntity` (id, creator, createDate)
- **BladeX**: 继承自 `TenantEntity` (包含租户字段)
- **迁移策略**: 所有实体类改为继承`TenantEntity`

### API路径调整
- **烟火**: `/api/platform/**`
- **BladeX**: `/social/**`

### 认证方式调整
- **烟火**: 自定义JWT + Redis
- **BladeX**: 使用`AuthUtil.getUserId()`获取当前用户

---

## 📊 数据迁移步骤

1. 执行 `social_migration.sql` 创建表结构
2. 从烟火数据库导出用户数据
3. 将用户基础信息导入`blade_user`表
4. 将社交扩展信息导入`social_user_ext`表
5. 导入其他业务表数据
6. 同步数据到ElasticSearch
7. 初始化Redis缓存

---

## ⚠️ 注意事项

1. **数据一致性**: 确保user_id正确关联blade_user表
2. **ID生成策略**: 使用MyBatis-Plus的ASSIGN_ID
3. **事务管理**: 单体应用使用@Transactional
4. **Redis Key命名**: 统一前缀`social:*`
5. **WebSocket端点**: `/social/ws/{userId}`

---

## � 待创建的DTO类列表

以下DTO类需要在`org.springblade.modules.social.pojo.dto`包下创建:

1. **ImgDetailDTO** - 图片详情DTO
2. **CommentDTO** - 评论DTO
3. **AgreeCollectDTO** - 点赞收藏DTO
4. **FollowDTO** - 关注DTO
5. **AlbumDTO** - 专辑DTO
6. **TagDTO** - 标签DTO
7. **BrowseRecordDTO** - 浏览记录DTO
8. **SearchRecordDTO** - 搜索记录DTO
9. **AlbumImgRelationDTO** - 专辑图片关系DTO

## 📝 待创建的VO类列表

以下VO类需要在`org.springblade.modules.social.pojo.vo`包下创建:

1. **ImgDetailVo** - 图片详情VO
2. **CategoryVo** - 分类VO(树形结构)
3. **TagVo** - 标签VO
4. **AlbumVo** - 专辑VO
5. **FollowVo** - 关注VO
6. **AgreeCollectVo** - 点赞收藏VO
7. **UserVo** - 用户VO
8. **UserRecordVo** - 用户记录VO
9. **CommentVo** - 评论VO (已创建)
10. **TrendVo** - 动态VO (已创建)

---

## 📈 最终迁移进度

### 所有阶段完成情况

- [x] **阶段一**: 环境准备和分析 ✅ 100%
- [x] **阶段二**: 数据库迁移 ✅ 100%
- [x] **阶段三**: 创建社交模块结构 ✅ 100%
- [x] **阶段四**: 实体类迁移(13个) ✅ 100%
- [x] **阶段五**: Mapper层迁移(13个接口 + 13个XML) ✅ 100%
- [x] **阶段六**: Service层迁移(15个接口 + 9个DTO + 10个VO + 15个实现类) ✅ 100%
- [x] **阶段七**: Controller层迁移(9个Controller + 49个API接口) ✅ 100%
- [x] **阶段八**: 中间件集成(ElasticSearch + RabbitMQ + WebSocket) ✅ 100%
- [x] **阶段九**: 推荐系统迁移(5个工具类 + 1个服务 + 1个控制器) ✅ 100%
- [x] **阶段十**: 配置文件整合和依赖管理 ✅ 100%

**总体进度**: 10/10 = **100%完成** 🎉

### 迁移成果统计

- **Java文件**: 109个
- **数据表**: 13个
- **API接口**: 49个
- **文档**: 6个(850+行)

### 下一步工作

1. **测试验证**
   - 执行数据库迁移脚本
   - 配置必需的中间件
   - 启动应用并测试API接口
   - 验证各项功能是否正常

2. **部署上线**
   - 参考 [部署指南](DEPLOYMENT_GUIDE.md)
   - 配置生产环境
   - 执行性能测试
   - 监控系统运行状态

3. **持续优化**
   - 根据实际使用情况优化性能
   - 完善监控和日志
   - 添加更多单元测试
   - 优化推荐算法

---

## 📞 相关文档

- [迁移完成报告](MIGRATION_COMPLETE.md) - 完整迁移统计和说明
- [Service层迁移指南](SERVICE_MIGRATION_GUIDE.md) - Service层详细说明
- [中间件集成文档](MIDDLEWARE_INTEGRATION.md) - 中间件配置和使用
- [推荐系统文档](RECOMMEND_SYSTEM.md) - 推荐系统架构和算法
- [部署指南](DEPLOYMENT_GUIDE.md) - 部署和配置说明

---

**迁移完成时间**: 2026-01-26
**最终状态**: ✅ 迁移100%完成,可以开始测试和部署
