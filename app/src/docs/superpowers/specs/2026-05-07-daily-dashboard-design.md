# 日常仪表盘应用 — 设计文档

## 概述

一个个人日常管理中心，集天气、日程、重大日期、待办、CSGO 赛事、课程表于一体。独立于现有课程表项目，复用其技术栈与 Firebase 基础设施。

## 技术栈

| 层 | 技术 |
|---|------|
| 框架 | Vue 3 + TypeScript |
| 构建 | Vite |
| UI | Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router |
| 后端 / 数据库 | Firebase Auth + Firestore |
| 日期处理 | dayjs |
| 天气 API | OpenWeatherMap |
| 电竞 API | PandaScore |

## 项目结构

```
daily-dashboard/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── public/
│   ├── favicon.ico
│   └── csgo-events.json              # CSGO 赛事兜底数据
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── env.d.ts
│   ├── firebase/
│   │   └── init.ts                    # Firebase 初始化
│   ├── router/
│   │   └── index.ts                   # 路由配置 + 守卫
│   ├── types/
│   │   └── index.ts                   # 全局类型定义
│   ├── utils/
│   │   ├── weather.ts                 # OpenWeatherMap API 封装
│   │   └── pandascore.ts              # PandaScore API 封装
│   ├── stores/
│   │   ├── auth.ts                    # 认证状态
│   │   ├── calendar.ts                # 日程 store
│   │   ├── todo.ts                    # 待办 store
│   │   ├── importantDates.ts          # 重大日期 store
│   │   ├── csgo.ts                    # CSGO 赛事 store
│   │   ├── weather.ts                 # 天气 store
│   │   ├── course.ts                  # 课程表 store（移植）
│   │   ├── semester.ts               # 学期 store（移植）
│   │   └── timeSlot.ts               # 时间段 store（移植）
│   ├── views/
│   │   ├── LoginView.vue              # 登录
│   │   ├── RegisterView.vue           # 注册
│   │   ├── NotFoundView.vue           # 404
│   │   ├── DashboardView.vue          # 仪表盘主页
│   │   ├── CalendarView.vue           # 日程详情
│   │   ├── TodoView.vue               # 待办详情
│   │   ├── ImportantDatesView.vue     # 重大日期管理
│   │   ├── CourseGridView.vue         # 完整课表（移植）
│   │   └── SettingsView.vue           # 设置
│   └── components/
│       ├── layout/
│       │   ├── AppSidebar.vue          # 侧边栏导航
│       │   └── AppTopBar.vue           # 顶部栏
│       ├── widgets/
│       │   ├── WeatherWidget.vue       # 天气卡片
│       │   ├── ScheduleWidget.vue      # 日程/课表卡片（含 Tab）
│       │   ├── ImportantDatesWidget.vue # 重大日期卡片
│       │   ├── TodoWidget.vue          # 待办卡片
│       │   └── CsgoWidget.vue          # CSGO 赛事卡片
│       └── course/                     # 课程表相关组件（移植）
│           ├── CourseGrid.vue
│           ├── CourseCard.vue
│           ├── GridHeader.vue
│           ├── TimeColumn.vue
│           ├── CourseDialog.vue
│           └── TimeSlotEditor.vue
```

## 路由设计

| 路径 | 视图 | 说明 | 鉴权 |
|------|------|------|------|
| `/login` | LoginView | 登录 | 游客 |
| `/register` | RegisterView | 注册 | 游客 |
| `/` | DashboardView | 仪表盘主页 | 需登录 |
| `/calendar` | CalendarView | 日程详情 | 需登录 |
| `/todo` | TodoView | 待办详情 | 需登录 |
| `/dates` | ImportantDatesView | 重大日期 | 需登录 |
| `/course` | CourseGridView | 完整课表 | 需登录 |
| `/settings` | SettingsView | 设置 | 需登录 |
| `/:pathMatch(.*)*` | NotFoundView | 404 | 无限制 |

路由守卫逻辑：
- `meta: { requiresAuth: true }` 的页面，未登录时重定向到 `/login`
- 已登录用户访问 `/login` 或 `/register` 时重定向到 `/`

## 仪表盘主页布局

屏幕分为上下两个区域：

**顶部概览栏（横向）**：天气 + 当前日期 + 今日摘要
```
┌──────────────────────────────────────────────────────────┐
│  ☀️ 北京 25°C 多云   体感 22°C     📅 2026年5月7日 周三  │
│                                                    今日: 2节课 · 3项待办 │
└──────────────────────────────────────────────────────────┘
```

**主内容区（2 列网格）**：四个 widget 卡片
```
┌──────────────────────────────┬────────────────────────────┐
│ 📅 日程/课表 (Tab: 今日|本周) │ 🎯 重大日期                │
│ 今日课表/日程列表             │ 生日 - 6月1日 (25天后)     │
│                              │ 毕业典礼 - 7月5日 (59天后)│
│ [查看完整课表 →]              │ [管理 →]                  │
├──────────────────────────────┼────────────────────────────┤
│ ✅ 待办事项                   │ 🎮 CSGO 重大赛事           │
│ □ 买教材                     │ IEM Cologne 2026           │
│ □ 复习期末                   │   7/20 - 8/4  🔥剩74天    │
│ [+ 添加]                     │ ESL Pro League S21         │
│                              │   9/1 - 9/22              │
│                              │ [LIVE] PGL Major 2026     │
│                              │ (比赛进行中)               │
└──────────────────────────────┴────────────────────────────┘
```

1080p 下一屏可见，无需滚动。窄屏自动变为单列。

## 各模块设计

### 1. 天气模块

- **数据源**：OpenWeatherMap Free API（60次/分钟免费）
- **展示内容**：城市、当前温度、体感温度、天气图标、湿度、未来 3 天预报
- **缓存策略**：localStorage 缓存 30 分钟，避免频繁请求
- **配置**：Settings 页面可设置城市

### 2. 日程 & 课表模块

- **Dashboard 卡片**：两个 Tab
  - 「今日」— 当日课程 + 自定义日程事件混排，按时段排序
  - 「本周」— 缩略周课表（移植 CourseGrid 组件适配为卡片尺寸）
- **CalendarView**：完整的日/周日程管理，支持 CRUD
- **数据源**：Firestore（日程）、Firestore（课程，移植）
- **课表说明**：从原课程表项目完整移植 CourseGrid、CourseCard、GridHeader、TimeColumn 等组件和 stores，Firestore 数据结构保持一致

### 3. 重大日期模块

- **Dashboard 卡片**：显示最近 3-5 个重大日期 + 倒计时
- **ImportantDatesView**：完整的日期管理页，增删改
- **数据字段**：名称、日期、颜色标记、类型（生日/纪念日/自定义）
- **排序**：按日期升序，已过去的不显示
- **数据源**：Firestore

### 4. 待办模块

- **Dashboard 卡片**：只显示「未完成 + 今天截止」的任务，已完成项不出现
- **TodoView**：完整列表，可查看所有/已完成/未完成，支持增删改
- **数据字段**：内容、是否完成、截止日期、创建时间
- **数据源**：Firestore

### 5. CSGO 赛事模块

- **数据源**：PandaScore API（主）+ localStorage 缓存 + public/csgo-events.json（兜底）
- **展示内容**：赛事名称、起止日期、倒计时、赛事 Logo
- **LIVE 标识**：正在进行的赛事显示 [LIVE] 标签
- **排序**：即将开始的优先，已结束的隐藏
- **Dashboard 卡片**：显示最近 3-5 个赛事
- **缓存策略**：PandaScore 数据缓存 6 小时（赛事数据更新不频繁）

### 6. 设置模块

- 天气城市配置
- 深色模式切换（沿用课程表的实现）
- 用户登出

## 数据结构（Firestore）

```
users/{uid}/
├── settings/
│   ├── city: string          # 天气城市
│   ├── darkMode: boolean
│   └── ...
├── calendar/{id}
│   ├── title: string
│   ├── date: Timestamp
│   ├── time: string          # 可选
│   └── note: string
├── todos/{id}
│   ├── content: string
│   ├── done: boolean
│   ├── dueDate: Timestamp    # 可选
│   └── createdAt: Timestamp
├── importantDates/{id}
│   ├── name: string
│   ├── date: Timestamp
│   ├── color: string
│   ├── type: 'birthday' | 'anniversary' | 'custom'
│   └── note: string
├── semesters/{id}
│   └── ...                   # 移植，结构不变
├── courses/{id}
│   └── ...                   # 移植，结构不变
└── timeSlots/{id}
    └── ...                   # 移植，结构不变
```

## CSGO 数据兜底文件

`public/csgo-events.json` 格式：

```json
[
  {
    "id": "iem-cologne-2026",
    "name": "IEM Cologne 2026",
    "startDate": "2026-07-20",
    "endDate": "2026-08-04",
    "logo": "https://example.com/logo.png"
  }
]
```

## 与课程表项目的差异

| 项目 | 课程表（原） | 仪表盘（新） |
|------|------------|------------|
| 定位 | 单一课程管理 | 日常信息总览 |
| 首页 | 课表网格 | Dashboard 看板 |
| 导航 | 课表 + 设置 | 仪表盘/日程/待办/重大日期/课表/设置 |
| 新增模块 | 无 | 天气、日程、待办、重大日期、CSGO |
| 数据范围 | Firestore 课程数据 | Firestore 多集合 + 外部 API |

## 实施计划

### Phase 1 — 项目骨架
1. 脚手架项目（Vite + Vue 3 + TS）
2. 接入 Firebase Auth + Firestore
3. 路由器 + 守卫
4. 布局组件（Sidebar + TopBar）

### Phase 2 — 核心功能
5. 课程表模块移植（stores + 组件 + 视图）
6. 日程管理（store + CalendarView）
7. 待办管理（store + TodoView）
8. 重大日期管理（store + ImportantDatesView）

### Phase 3 — Dashboard & API
9. 仪表盘主页布局 + 各 Widget
10. 天气模块（OpenWeatherMap）
11. CSGO 赛事模块（PandaScore + 兜底）

### Phase 4 — 收尾
12. 设置页面
13. 深色模式适配
14. 响应式优化
