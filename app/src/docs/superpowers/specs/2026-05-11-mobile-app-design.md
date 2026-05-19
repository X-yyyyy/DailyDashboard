# 单杀donk了吗 — 移动端 Android 应用设计

## 概述

基于现有 Web 端（Vue 3 + TypeScript + Firebase）的 Capacitor Android 应用。复用全部 stores / utils / types / Firebase 逻辑，新增独立移动端视图层。

## 技术方案

| 层 | 选型 |
|--------|--------|
| 混合框架 | Capacitor 6 (Android) |
| 前端 | Vue 3 + TypeScript + Vite |
| 移动端 UI | Vant 4 (有赞 Vue 3 组件库) |
| 桌面端 UI | Element Plus（不变） |
| 状态管理 | Pinia（复用现有 stores） |
| 后端 | Firebase Auth + Firestore（复用） |

## 架构

```
src/
├── mobile/
│   ├── layout/
│   │   ├── MobileLayout.vue      # 底部 TabBar + 内容区
│   │   └── MobileNavBar.vue      # 顶部导航栏
│   ├── views/
│   │   ├── HomeView.vue          # 首页概览
│   │   ├── ScheduleView.vue      # 日程（日期选择 + 待办）
│   │   ├── CourseView.vue        # 课表（周课表 + 编辑）
│   │   ├── CsgoView.vue          # CSGO 赛事
│   │   └── SettingsView.vue      # 设置
│   ├── components/
│   │   ├── WeekBar.vue           # 7 天日期栏
│   │   ├── MonthPicker.vue       # 月历下拉
│   │   ├── CourseCard.vue        # 移动端课程卡片
│   │   └── CsgoCard.vue          # 赛事卡片
│   └── router.ts                 # 移动端子路由
├── stores/    (复用)
├── utils/     (复用)
├── types/     (复用)
└── router/
    └── index.ts                  # 添加移动端检测重定向
```

## 路由

| 路径 | 页面 | Tab |
|------|------|-----|
| `/m/home` | 首页 | 🏠 |
| `/m/schedule` | 日程 | 📋 |
| `/m/course` | 课表 | 📚 |
| `/m/csgo` | 赛事 | 🎮 |
| `/m/settings` | 设置 | ⚙️ |

Capacitor 环境下访问 `/` 自动重定向到 `/m/home`，桌面浏览器不受影响。

## 底部导航（5 Tab）

| Tab | 图标 | 页面 |
|-----|------|------|
| 首页 | 🏠 | 今日概览 |
| 日程 | 📋 | 日期选择 + 待办 |
| 课表 | 📚 | 周课表 + 编辑 |
| 赛事 | 🎮 | CSGO 赛事列表 |
| 设置 | ⚙️ | 学期 / 城市 / 深色模式 / 登出 |

## 页面详细设计

### 1. 首页（HomeView）

天气 → 今日待办 → 重大日期倒计时 → 今日课程 → LIVE 赛事

| 区块 | 内容 | 数据源 |
|------|------|--------|
| 天气 | 城市、温度、体感、湿度、图标 | weatherStore |
| 今日待办 | 未完成待办列表，可勾选 + 快速添加 | todoStore |
| 重大日期 | 最近倒计时（名称 + 天数） | importantDateStore |
| 今日课程 | 当天课程预览（时间 + 名称 + 教室） | courseStore + timeSlotStore |
| LIVE 赛事 | 进行中的 CSGO 赛事（名称 + 队伍 + 时间） | csgoStore |

### 2. 日程（ScheduleView）

```
顶部：左 5月11日 周一 | 右 📅 下拉按钮（打开月历）
中部：7 天日期栏（可点击切换，高亮当天）
底部：选中日期的待办清单 + 快速添加
```

- 点击 📅 打开 Vant Calendar 组件，选择日期后跳转
- 7 天栏显示 周一到周日，日期数字，当天高亮主题色
- 底部待办根据选中日期过滤，可勾选、添加

### 3. 课表（CourseView）

```
顶部：第 X 周  ←  →
主体：周课表 Grid（7 列 × 时间行）
点击课程：弹出 ActionSheet 显示课程详情 → 编辑 / 删除
```

- 顶部左右箭头切换周次（复用 semesterStore）
- 课程过滤规则与桌面端一致（isScheduleVisible）
- 移动端紧凑显示：颜色标记 + 课程名 + 教室
- 编辑使用 Vant Popup/Dialog 表单（名称、教室、颜色、时段、周次类型）

### 4. 赛事（CsgoView）

三个分组：

| 分组 | 条件 | 显示内容 |
|------|------|---------|
| LIVE | 当前时间在赛事区间内 | 🔴 名称 + 队伍 vs 队伍 + 比赛时间 |
| 即将开始 | startDate > now | 名称 + 队伍 + 📅 日期时间 |
| 往期 | endDate < now | 灰色显示名称 + 队伍 + 日期 |

### 5. 设置（SettingsView）

| 项目 | 交互 | 数据源 |
|------|------|--------|
| 学期设置 | 点击进入编辑（名称 + 起始日期 + 总周数） | semesterStore |
| 城市 | 输入城市名 | weatherStore |
| 深色模式 | Switch 开关 | 持久化到 Firestore |
| 登出 | 红色按钮，确认后退出 | authStore |

## 数据流

所有移动端页面直接复用现有 Pinia stores，不新增任何 store。移动端只新增 view/component 层。

```
App.vue (检测 Capacitor 环境)
  └─ 是 → MobileLayout (Vant TabBar)
  └─ 否 → 现有桌面端路由
```

