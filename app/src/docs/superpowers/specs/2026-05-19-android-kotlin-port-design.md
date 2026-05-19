# Android Kotlin 移植方案 — Daily Dashboard

## 概述

将现有的 Vue.js + TypeScript 全栈「大学生每日仪表盘」移植为 Android 原生 App。目标平台为 Android，技术栈 Kotlin + Jetpack Compose + Firebase，服务 1-2 人日常使用。

## 技术栈

| 层面 | 选型 | 理由 |
|------|------|------|
| 语言 | Kotlin 2.0+ | 官方推荐，Compose Compiler 已合并进 Kotlin 仓库 |
| UI | Jetpack Compose + Material 3 | 声明式 UI，Modern Android Development |
| 导航 | Navigation Compose (Type-Safe) | `sealed interface` + `@Serializable` 路由，无字符串拼写 |
| 后端 | Firebase REST API | 华为 P60 无 GMS，无法使用 Firebase Android SDK，改用 REST 接口 |
| 网络 | Retrofit + OkHttp | 调用 Firebase REST API + OpenWeatherMap / PandaScore API |
| DI | Koin | 轻量，无注解处理器，适合小项目 |
| 缓存 | DataStore（Firestore 数据 + CS:GO JSON） | 不引入 Room；Firestore 数据定时轮询缓存到 DataStore |
| 图片 | Coil | Compose 原生支持，轻量 |
| 构建 | Gradle Kotlin DSL + Version Catalogs | 现代 Gradle 最佳实践 |
| 通知 | Firebase Cloud Functions + FCM | 避免 Android 后台保活问题 |

## 架构

### 分层

```
com.dailydashboard.app/
├── MainActivity.kt              # 单 Activity，全局 Scaffold
├── DailyDashboardApp.kt         # Koin Application
├── navigation/
│   ├── Screen.kt                # 类型安全路由定义
│   ├── AppNavGraph.kt           # 导航图
│   └── BottomNavItem.kt         # 底部 Tab 定义
├── data/
│   ├── model/                   # 数据模型
│   ├── remote/                  # Firebase REST API + Retrofit Service 定义
│   ├── local/                   # DataStore 缓存辅助
│   └── repository/              # Repository 层
├── ui/
│   ├── theme/                   # Material 3 主题（鼠尾绿暖色调）
│   ├── components/              # 通用组件
│   ├── screens/                 # 页面
│   │   ├── auth/                # 登录页
│   │   ├── dashboard/           # 首页仪表盘
│   │   ├── calendar/            # 日程
│   │   ├── course/              # 课表
│   │   ├── csgo/                # CS:GO 赛事
│   │   ├── todo/                # 待办
│   │   ├── importantdates/      # 重要日期
│   │   └── settings/            # 设置
│   └── viewmodel/               # ViewModel
├── di/
│   ├── AppModule.kt             # 全局依赖
│   ├── FirebaseModule.kt        # Firebase 依赖
│   └── RepositoryModule.kt      # Repository 绑定
└── util/
    ├── SemesterCalculator.kt    # 学期周数计算
    ├── WeatherCache.kt          # 天气缓存（30min TTL）
    └── Constants.kt             # 常量（颜色、默认配置）
```

### 架构原则

- **MVVM**：Screen (Composable) ← ViewModel (StateFlow) ← Repository
- **数据拉取方式**：Firebase 走 REST API，不支持实时监听；使用下拉刷新 + 定时轮询（Dashboard 页面自动刷新）
- **部分加载**：Dashboard 各 Widget 独立加载，慢接口不阻塞首页渲染
- **Domain 层极轻**：纯工具函数放 util，不强制 UseCase 模式
- **Repository 封装缓存逻辑**：使用 DataStore 缓存 Firestore 数据，App 冷启动时优先展示缓存再后台刷新

## 数据层

### Firestore 数据结构

```
users/{uid}/
├── courses/{courseId}
├── timeSlots/{slotId}
├── calendar/{eventId}
├── todos/{todoId}
├── importantDates/{dateId}
└── semester/current        # 单文档
```

### 数据模型

```kotlin
data class Course(
    val id: String = "",
    val name: String = "",
    val teacher: String = "",
    val location: String = "",
    val type: String = "",          // "required" | "elective"
    val color: String = "",
    val schedules: List<Schedule> = emptyList()
)

data class Schedule(
    val dayOfWeek: Int = 1,         // 1-7
    val startSlot: Int = 1,
    val duration: Int = 1,
    val weekType: String = "all",   // "all" | "odd" | "even" | "custom"
    val customWeeks: List<Int> = emptyList()
)

data class TodoItem(
    val id: String = "",
    val content: String = "",
    val done: Boolean = false,
    val dueDate: String? = null,    // ISO 8601 string
    val createdAt: String? = null
)
```

> 注：使用 REST API，`id` 字段手动从 Firestore 返回的 `document name` 中提取。

### 数据来源策略

| 数据类型 | 来源 | 缓存策略 |
|---------|------|---------|
| 课程/日程/待办/重要日期/学期 | Firebase REST API | DataStore 缓存 + 定时刷新（Dashboard 自动轮询，其他页面下拉触发） |
| 天气 (OpenWeatherMap) | Retrofit | ViewModel 层 30min TTL |
| CS:GO 赛事 (PandaScore) | Retrofit | DataStore JSON 缓存 2h TTL |

### 认证与安全

- **登录方式**：Firebase REST API 邮箱/密码登录，获取 idToken
- **所有请求**：在 Header 中携带 `Authorization: Bearer {idToken}`
- **Firestore Rules**：UID 白名单

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if uid in ['your-uid', 'friend-uid'];
    }
  }
}
```

## UI / 导航

### 底部 Tab（5 个）

| Tab | 路由 | 内容 |
|-----|------|------|
| 首页 | `Dashboard` | 天气卡片 + 今日课程/日程 + 待办 Top5 + 倒计时 + CS:GO 预告 |
| 日程 | `Calendar` | 日/周视图日程管理 |
| 课表 | `Course` | 学期课表网格 + 周导航 + 课程 CRUD（ModalBottomSheet）|
| 赛事 | `CSGO` | 分组展示进行中/即将到来/已结束比赛 + 订阅通知 |
| 设置 | `Settings` | 学期配置 / 城市选择 / 深色模式 |

### 非 Tab 页面

- `Login` / `Register` — 认证
- `TodoList` — 完整待办列表
- `ImportantDates` — 完整重要日期
- `CourseEdit(courseId: String?)` — 课程编辑页（如 ModalBottomSheet 不够用时）

### 导航实现

- **类型安全路由**：`sealed interface Screen` + `@Serializable`
- **全局单 Scaffold**：通过 `currentBackStackEntryAsState()` 判断当前路由，控制 BottomBar 显隐

## 主题

继承 Web 版的鼠尾绿/暖色调中性色风格：

- **Primary** — `#8A9A5B` (鼠尾绿)
- **Background** — 暖白中性色
- **Material Theme Builder** 生成完整亮/暗色板
- 深色模式通过 Settings 切换，持久化到 DataStore

## 阶段计划

### Phase 1：项目骨架（~1 周）

- [ ] 创建 Android 项目，配置 Version Catalogs
- [ ] Kotlin 2.0 + Compose 插件配置
- [ ] Material 3 主题实现
- [ ] Type-Safe Navigation 框架
- [ ] 全局 Scaffold + BottomBar
- [ ] Firebase REST API 客户端封装（Auth + Firestore CRUD，基于 OkHttp/Retrofit）
- [ ] Koin 模块注册
- [ ] 邮箱/密码登录页 + Token 持久化（DataStore）
- [ ] Firestore Rules 配置

### Phase 2：课表系统（~1.5 周）

- [ ] Course 数据模型 + Repository
- [ ] 课表网格 Custom Layout（处理跨节课程）
- [ ] 周导航 + 学期配置
- [ ] ModalBottomSheet 课程编辑
- [ ] TimeSlot 管理

### Phase 3：日常工具（~1.5 周）

- [ ] Todo CRUD + 列表/筛选
- [ ] Calendar 日/周视图
- [ ] ImportantDates 倒计时
- [ ] Dashboard 首页聚合（Flow.combine）
- [ ] 骨架屏 / 部分加载状态

### Phase 4：外部 API（~1 周）

- [ ] Retrofit + OpenWeatherMap 天气卡片
- [ ] Retrofit + PandaScore CS:GO 列表
- [ ] DataStore 赛事缓存 + 备用数据
- [ ] Cloud Functions + FCM 比赛通知

### Phase 5：打磨收尾（~0.5 周）

- [ ] 深色模式完善
- [ ] 设置页面
- [ ] 统一错误/加载/空状态
- [ ] 性能优化
- [ ] 打包 APK

**预估总工期：4-6 周（每日 2-3 小时）**

## 注意事项

1. **课表网格**是 UI 最复杂的部分，建议用 Compose Custom Layout 手写，避免 Row/Column 嵌套处理跨节课程
2. **Dashboard 慢接口**（天气/CS:GO）用骨架屏，不阻塞本地数据渲染
3. **Firestore 缓存**用 DataStore + JSON，App 冷启动先展示缓存数据再后台刷新
4. **CS:GO 缓存**用 DataStore 存 JSON + 时间戳，2h TTL
5. **通知用 Cloud Functions + FCM**，不在 Android 端保活
6. **Firebase REST API 的 idToken 有效期 1 小时**，需在 Repository 层做自动刷新