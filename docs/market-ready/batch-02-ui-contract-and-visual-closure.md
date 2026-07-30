# 第二批：小程序接口契约、视觉回归与交互收口

更新日期：2026-07-30  
适用分支：`refactor/market-ready-phase1`

## 一、本批目标

本批不重新发明页面风格，而是在恢复原项目成熟布局和信息密度的基础上，接入本轮真实接口、状态和权限。禁止通过多字段别名、Mock 数据或固定尺寸图片掩盖后端契约问题。

## 二、已完成代码范围

### 1. 统一请求和真实字段

- GET 参数统一过滤 `undefined`、`null`、空字符串以及字符串形式的 `undefined/null`。
- 场馆、培训、推荐、喜欢、收藏和关注流改用后端正式字段。
- 场馆分页只发送 `current/size`，类型筛选只发送 `typeId`。
- 培训课程只使用 `title、coverImage、courseType、contentMode、purchaseRequired、totalLessons` 等正式字段。
- 关注动态按后端 `TrendVO` 使用 `mid、imgsUrl、cover、content、userId、username、avatar、agreeCount、commentCount、isAgree、time`。

### 2. 首页推荐

- 下拉刷新才清空瀑布流。
- 上拉分页只追加新记录，不再调用 `waterfall.clear()` 重排整页。
- 增加请求版本号，防止较慢旧请求覆盖新列表。
- 曝光、点击和不感兴趣使用后端推荐反馈接口。

### 3. 场馆频道

- 分类 Tab 改为单行固定高度胶囊样式。
- 支持横向滚动、选中项自动进入可视区域、长分类名省略。
- “全部”不发送 `typeId`，具体分类只发送真实 `typeId`。
- 支持全部、1km、3km、5km、距离优先。
- 无法获得距离的场馆在半径筛选中排除，在全部列表中排在有距离场馆之后。
- 类型接口失败不会销毁固定的“全部”入口。

### 4. 达人、赛事与培训首页

- 达人频道恢复横向达人列表和双列教程瀑布流。
- 赛事频道恢复紧凑横向图文卡片，而非大尺寸纵向后台式卡片。
- 培训首页改用正式课程、机构和教练字段。
- 体验课只按字典中的真实 `courseType` 查询，不再发送 `course_type/isExperience`。

### 5. 图片和内容卡片

- 通用内容卡片不再把单图强制设为 `420rpx + aspectFit`。
- 单图使用自然比例，网格多图使用固定网格。
- 统一经过媒体 URL 规范化；加载失败显示空状态，不用假图掩盖。
- 页面布局不依赖 `AppImage` 额外包装层决定高度。

### 6. 个人中心、喜欢、收藏和关注

- 顶部安全区按状态栏与胶囊真实尺寸计算，不再重复累加顶部间距。
- “我的服务”恢复图标式入口，去掉“赛、兑、课”等文字占位图标。
- 内容 Tab 恢复为“作品 / 喜欢 / 收藏”。
- 收藏指内容收藏，不恢复相册/专辑社交。
- 喜欢与收藏按后端 `AgreeCollectVO` 正式字段展示。
- 关注动态修复封面、分页追加、重复请求和空状态覆盖问题。
- 已认证达人显示“达人工作台”入口。

## 三、关键文件

### 小程序

- `utils/request.js`
- `api/venue.js`
- `api/training.js`
- `api/training-org.js`
- `api/training-teacher.js`
- `pages/index/components/recommend-page.vue`
- `pages/index/components/venue-page.vue`
- `pages/index/components/talent-page.vue`
- `pages/index/components/competition-page.vue`
- `pages/index/components/training-page.vue`
- `pages/index/interest/interest.vue`
- `components/PostCard/PostCard.vue`
- `components/Collection/Collection.vue`
- `components/Collection/CollectionImg.vue`
- `pages/user/user.vue`

## 四、自动检查

- `miniapp-script-syntax.yml` 覆盖上述核心 Vue 页面脚本语法。
- `product-closure-check.yml` 阻止正式页面重新出现 Mock、字段别名、重复分页参数、旧预约占位、临时补丁工作流和误导性支付入口。

## 五、仍需真实环境验证

- HBuilderX 编译和微信开发者工具 WXML 编译。
- 不同胶囊位置、全面屏和普通屏顶部间距。
- 场馆定位授权拒绝、首次授权、定位失败和无经纬度数据。
- 推荐、达人和关注瀑布流连续加载 3 页以上的滚动位置。
- 正式 OSS/MinIO 图片、视频封面和失败地址。
- 喜欢、收藏、取消喜欢后列表实时变化。

静态检查和代码提交不能替代微信开发者工具与真机验收。
