# 移动端 Android 应用实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build independent mobile view layer (Vant 4) for the existing daily dashboard, packaged as Capacitor Android app.

**Architecture:** Reuse all existing Pinia stores, utils, and Firebase logic. Add `src/mobile/` directory with independent view components and a dedicated MobileLayout with bottom TabBar. Desktop web app unchanged.

**Tech Stack:** Vue 3 + TypeScript + Vite + Vant 4 + Capacitor 6 (Android)

---

## File Structure

```
src/
├── mobile/
│   ├── layout/
│   │   └── MobileLayout.vue          # Bottom TabBar + NavBar + router-view
│   ├── views/
│   │   ├── HomeView.vue              # Overview: weather, todo, dates, courses, live csgo
│   │   ├── ScheduleView.vue          # Date picker + todo list filtered by day
│   │   ├── CourseView.vue            # Week grid + tap to edit/delete
│   │   ├── CsgoView.vue              # LIVE / Upcoming / Past grouped list
│   │   └── SettingsView.vue          # Semester, city, dark mode, logout
│   ├── components/
│   │   ├── MobileNavBar.vue          # Top nav bar (title + optional back)
│   │   └── CourseEditPopup.vue       # Course add/edit form in popup
│   └── router.ts                     # Mobile route definitions
├── router/
│   └── index.ts                      # Modified: add /m/* routes, Capacitor redirect
├── App.vue                           # Modified: detect Capacitor, redirect
└── utils/
    └── platform.ts                   # New: isNativeApp() helper
```

New files: `src/mobile/` (11 files), `src/utils/platform.ts`
Modified: `src/router/index.ts`, `src/App.vue`
Package: add `vant`, `@capacitor/core`, `@capacitor/cli`, `@capacitor/android`

---

### Task 1: Install Dependencies & Create Platform Utility

**Files:**
- Modify: `package.json`
- Create: `src/utils/platform.ts`

- [ ] **Step 1: Install Vant and Capacitor**

```bash
npm install vant
npm install -D @capacitor/cli
npx cap init --webDir dist "单杀donk了吗"
npm install @capacitor/core @capacitor/android
```

Expected: `package.json` updated with new dependencies.

- [ ] **Step 2: Create platform detection utility**

```typescript
// src/utils/platform.ts
export function isNativeApp(): boolean {
  if (typeof window === 'undefined') return false
  return !!(window as any).Capacitor?.isNativePlatform()
}
```

- [ ] **Step 3: Commit**

```bash
git add package.json package-lock.json src/utils/platform.ts
git commit -m "feat: add vant capacitor deps and platform util"
```

---

### Task 2: Mobile Router & Layout

**Files:**
- Create: `src/mobile/router.ts`
- Modify: `src/router/index.ts`
- Create: `src/mobile/layout/MobileLayout.vue`

- [ ] **Step 1: Create mobile route definitions**

```typescript
// src/mobile/router.ts
import type { RouteRecordRaw } from 'vue-router'

export const mobileRoutes: RouteRecordRaw = {
  path: '/m',
  meta: { requiresAuth: true },
  children: [
    { path: '', redirect: '/m/home' },
    { path: 'home', component: () => import('@/mobile/views/HomeView.vue') },
    { path: 'schedule', component: () => import('@/mobile/views/ScheduleView.vue') },
    { path: 'course', component: () => import('@/mobile/views/CourseView.vue') },
    { path: 'csgo', component: () => import('@/mobile/views/CsgoView.vue') },
    { path: 'settings', component: () => import('@/mobile/views/SettingsView.vue') },
  ],
}
```

- [ ] **Step 2: Add mobile routes to main router**

Edit `src/router/index.ts`:

```typescript
// Add import at top
import { mobileRoutes } from '@/mobile/router'

// Add inside routes array (after the '/' parent route's closing bracket, before the 404 catch-all)
// Insert this route record:
import { createRouter, createWebHistory } from 'vue-router'
import { getAuth } from 'firebase/auth'
import { mobileRoutes } from '@/mobile/router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ... existing routes ...
    mobileRoutes,
    // ... existing catch-all ...
  ],
  // ... rest unchanged
})
```

Insert `mobileRoutes` before the `/:pathMatch(.*)*` catch-all route.

- [ ] **Step 3: Create MobileLayout with bottom TabBar**

```vue
<!-- src/mobile/layout/MobileLayout.vue -->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSemesterStore } from '@/stores/semester'
import { useCourseStore } from '@/stores/course'
import { useTimeSlotStore } from '@/stores/timeSlot'
import { useTodoStore } from '@/stores/todo'
import { useCalendarStore } from '@/stores/calendar'
import { useImportantDateStore } from '@/stores/importantDates'
import { useCsgoStore } from '@/stores/csgo'
import { useWeatherStore } from '@/stores/weather'
import MobileNavBar from '@/mobile/components/MobileNavBar.vue'

const router = useRouter()
const route = useRoute()

const auth = useAuthStore()
const semesterStore = useSemesterStore()
const courseStore = useCourseStore()
const timeSlotStore = useTimeSlotStore()
const todoStore = useTodoStore()
const calendarStore = useCalendarStore()
const dateStore = useImportantDateStore()
const csgoStore = useCsgoStore()
const weatherStore = useWeatherStore()

onMounted(() => {
  semesterStore.fetchSemester()
  courseStore.fetchCourses()
  timeSlotStore.fetchTimeSlots()
  todoStore.fetchTodos()
  calendarStore.fetchEvents()
  dateStore.fetchDates()
  csgoStore.refresh()
  weatherStore.refresh()
})

const tabs = [
  { name: 'home', icon: 'home-o', label: '首页' },
  { name: 'schedule', icon: 'notes-o', label: '日程' },
  { name: 'course', icon: 'bar-chart-o', label: '课表' },
  { name: 'csgo', icon: 'tv-o', label: '赛事' },
  { name: 'settings', icon: 'setting-o', label: '设置' },
]

function onTabChange(name: string) {
  router.replace(`/m/${name}`)
}

function getTitle(): string {
  const map: Record<string, string> = {
    home: '首页',
    schedule: '日程',
    course: '课表',
    csgo: 'CSGO 赛事',
    settings: '设置',
  }
  const tabName = route.path.replace('/m/', '')
  return map[tabName] || '单杀donk了吗'
}
</script>

<template>
  <div class="mobile-container">
    <MobileNavBar :title="getTitle()" />
    <div class="mobile-content">
      <router-view />
    </div>
    <van-tabbar active-color="#5A6B4A" inactive-color="#8A8A7A" :model-value="route.path.replace('/m/', '')" @change="onTabChange">
      <van-tabbar-item v-for="tab in tabs" :key="tab.name" :name="tab.name" :icon="tab.icon">
        {{ tab.label }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.mobile-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--mobile-bg, #f5f5f0);
}
.mobile-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  padding-bottom: 4px;
}
</style>
```

- [ ] **Step 4: Create MobileNavBar component**

```vue
<!-- src/mobile/components/MobileNavBar.vue -->
<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<template>
  <van-nav-bar :title="title" />
</template>
```

- [ ] **Step 5: Commit**

```bash
git add src/mobile/router.ts src/mobile/layout/MobileLayout.vue src/mobile/components/MobileNavBar.vue src/router/index.ts
git commit -m "feat: mobile router and layout with bottom tabbar"
```

---

### Task 3: HomeView — Today's Overview

**Files:**
- Create: `src/mobile/views/HomeView.vue`

This view shows 5 sections vertically: Weather → Today's Todos → Countdowns → Today's Courses → Live CSGO Events.

- [ ] **Step 1: Create HomeView**

```vue
<!-- src/mobile/views/HomeView.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import { useWeatherStore } from '@/stores/weather'
import { useTodoStore } from '@/stores/todo'
import { useImportantDateStore } from '@/stores/importantDates'
import { useCourseStore } from '@/stores/course'
import { useTimeSlotStore } from '@/stores/timeSlot'
import { useSemesterStore } from '@/stores/semester'
import { useCsgoStore } from '@/stores/csgo'
import { toDate } from '@/types'
import type { TodoItem } from '@/types'

const weatherStore = useWeatherStore()
const todoStore = useTodoStore()
const dateStore = useImportantDateStore()
const courseStore = useCourseStore()
const timeSlotStore = useTimeSlotStore()
const semesterStore = useSemesterStore()
const csgoStore = useCsgoStore()

const todayTodos = computed(() =>
  todoStore.todos.filter(t => !t.done).slice(0, 8)
)

const upcomingDates = computed(() =>
  dateStore.dates
    .filter(d => {
      const dt = toDate(d.date)
      return dt && dt >= new Date()
    })
    .sort((a, b) => (toDate(a.date)?.getTime() ?? 0) - (toDate(b.date)?.getTime() ?? 0))
    .slice(0, 4)
)

const todayCourses = computed(() => {
  const dayOfWeek = dayjs().day() === 0 ? 7 : dayjs().day()
  const week = semesterStore.currentWeek
  return courseStore.courses.filter(c =>
    c.schedules?.some(s => s.dayOfWeek === dayOfWeek && courseStore.isScheduleVisible(s, week))
  ).map(c => {
    const sched = c.schedules!.find(s => s.dayOfWeek === dayOfWeek && courseStore.isScheduleVisible(s, week))
    const slot = timeSlotStore.timeSlots.find(ts => ts.slot === sched?.startSlot)
    return {
      name: c.name,
      location: c.location,
      color: c.color,
      time: slot ? `${slot.startTime}` : '',
    }
  }).sort((a, b) => a.time.localeCompare(b.time))
})

const liveEvents = computed(() =>
  csgoStore.events.filter(e => {
    const now = new Date()
    const start = e.startDate ? new Date(e.startDate) : null
    const end = e.endDate ? new Date(e.endDate) : null
    return start && end && start <= now && end >= now
  })
)

function toggleTodo(todo: TodoItem) {
  todoStore.toggleDone(todo.id!, todo.done)
}

function formatCountdown(d: any): string {
  const dt = toDate(d.date)
  if (!dt) return ''
  const diff = dayjs(dt).diff(dayjs(), 'day') + 1
  if (diff <= 0) return ''
  return `距离 ${d.name} 还有 ${diff} 天`
}
</script>

<template>
  <div class="home-view">
    <!-- Weather -->
    <van-cell-group inset class="section">
      <van-cell>
        <template #title>
          <div class="weather-row">
            <span class="weather-temp">{{ weatherStore.current?.temp ?? '--' }}°C</span>
            <span class="weather-city">{{ weatherStore.city }}</span>
            <span v-if="weatherStore.current" class="weather-desc">
              体感 {{ weatherStore.current.feelsLike }}°C · 湿度 {{ weatherStore.current.humidity }}%
            </span>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- Today's Todos -->
    <van-cell-group inset class="section">
      <template #title>
        <div class="section-header">
          <span>今日待办</span>
          <span class="section-badge">{{ todoStore.todos.filter(t => !t.done).length }}</span>
        </div>
      </template>
      <van-cell v-for="todo in todayTodos" :key="todo.id">
        <template #title>
          <div class="todo-row" :class="{ done: todo.done }">
            <van-checkbox :model-value="todo.done" @click="toggleTodo(todo)" shape="square" />
            <span class="todo-text">{{ todo.content }}</span>
          </div>
        </template>
      </van-cell>
      <van-cell v-if="todayTodos.length === 0">
        <template #title><span class="empty-text">没有待办</span></template>
      </van-cell>
    </van-cell-group>

    <!-- Countdown -->
    <van-cell-group inset class="section" v-if="upcomingDates.length">
      <template #title>即将到来</template>
      <van-cell v-for="d in upcomingDates" :key="d.id">
        <template #title>
          <div class="countdown-row">
            <div class="countdown-dot" :style="{ background: d.color }" />
            <span>{{ formatCountdown(d) }}</span>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- Today's Courses -->
    <van-cell-group inset class="section">
      <template #title>今日课程</template>
      <van-cell v-for="c in todayCourses" :key="c.name + c.time">
        <template #title>
          <div class="course-row">
            <div class="course-color" :style="{ background: c.color }" />
            <span class="course-time">{{ c.time }}</span>
            <span class="course-name">{{ c.name }}</span>
            <span class="course-location">{{ c.location }}</span>
          </div>
        </template>
      </van-cell>
      <van-cell v-if="todayCourses.length === 0">
        <template #title><span class="empty-text">今日无课</span></template>
      </van-cell>
    </van-cell-group>

    <!-- Live CSGO Events -->
    <van-cell-group inset class="section" v-if="liveEvents.length">
      <template #title>
        <div class="section-header"><span class="live-dot" /> 正在进行</div>
      </template>
      <van-cell v-for="e in liveEvents" :key="e.id">
        <template #title>
          <div class="csgo-row">
            <span class="csgo-match">{{ e.matchName }}</span>
          </div>
          <div class="csgo-teams">{{ e.team1 }} vs {{ e.team2 }}</div>
          <div class="csgo-time">{{ e.beginAt ? dayjs(e.beginAt).format('HH:mm') : '' }}</div>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<style scoped>
.home-view { padding-bottom: 16px; }
.section { margin-bottom: 12px; }
.section-header { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; }
.section-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 20px; height: 20px; border-radius: 50%;
  background: #5A6B4A; color: #fff; font-size: 11px;
}
.weather-row { display: flex; align-items: baseline; gap: 8px; flex-wrap: wrap; }
.weather-temp { font-size: 24px; font-weight: 700; color: #3D3D35; }
.weather-city { font-size: 14px; color: #5A6B4A; font-weight: 500; }
.weather-desc { font-size: 13px; color: #8A8A7A; }
.todo-row { display: flex; align-items: center; gap: 10px; }
.todo-row.done .todo-text { text-decoration: line-through; color: #B5B5A5; }
.todo-text { font-size: 14px; }
.empty-text { color: #B5B5A5; font-size: 13px; }
.countdown-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.countdown-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.course-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.course-color { width: 4px; height: 16px; border-radius: 2px; flex-shrink: 0; }
.course-time { color: #5A6B4A; font-weight: 500; min-width: 36px; }
.course-name { flex: 1; }
.course-location { color: #8A8A7A; font-size: 12px; }
.live-dot {
  display: inline-block; width: 8px; height: 8px;
  border-radius: 50%; background: #e74c3c; animation: pulse 1.5s infinite;
}
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
.csgo-row { display: flex; align-items: center; gap: 6px; }
.csgo-match { font-weight: 500; font-size: 14px; }
.csgo-teams { font-size: 12px; color: #5A6B4A; margin-top: 2px; }
.csgo-time { font-size: 12px; color: #8A8A7A; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/mobile/views/HomeView.vue
git commit -m "feat: home view with weather, todos, countdown, courses, live csgo"
```

---

### Task 4: ScheduleView — Date Selector + Todo List

**Files:**
- Create: `src/mobile/views/ScheduleView.vue`

- [ ] **Step 1: Create ScheduleView**

```vue
<!-- src/mobile/views/ScheduleView.vue -->
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { useTodoStore } from '@/stores/todo'
import type { TodoItem } from '@/types'
import { toDate } from '@/types'

const todoStore = useTodoStore()

dayjs.locale('zh-cn')

const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const showCalendar = ref(false)

const weekDays = computed(() => {
  const today = dayjs()
  return Array.from({ length: 7 }, (_, i) => {
    const d = today.startOf('week').add(i, 'day')
    return {
      date: d.format('YYYY-MM-DD'),
      dayName: d.format('ddd'),
      dayNum: d.format('D'),
      isToday: d.format('YYYY-MM-DD') === dayjs().format('YYYY-MM-DD'),
    }
  })
})

const filteredTodos = computed(() =>
  todoStore.todos.filter(t => {
    if (!t.dueDate) return false
    const dt = toDate(t.dueDate)
    return dt && dayjs(dt).format('YYYY-MM-DD') === selectedDate.value
  })
)

const newContent = ref('')

function selectDate(dateStr: string) {
  selectedDate.value = dateStr
}

function onCalendarConfirm(date: Date) {
  selectedDate.value = dayjs(date).format('YYYY-MM-DD')
  showCalendar.value = false
}

async function addTodo() {
  const content = newContent.value.trim()
  if (!content) return
  await todoStore.addTodo({
    content,
    done: false,
    dueDate: new Date(selectedDate.value),
    createdAt: new Date(),
  })
  newContent.value = ''
}

function toggleTodo(todo: TodoItem) {
  todoStore.toggleDone(todo.id!, todo.done)
}
</script>

<template>
  <div class="schedule-view">
    <!-- Date header -->
    <div class="date-header">
      <span class="date-display">{{ dayjs(selectedDate).format('M月D日 dddd') }}</span>
      <van-button icon="calendar-o" size="small" plain @click="showCalendar = true" />
    </div>

    <!-- Week bar -->
    <div class="week-bar">
      <div
        v-for="d in weekDays"
        :key="d.date"
        class="week-day"
        :class="{ active: d.date === selectedDate, today: d.isToday }"
        @click="selectDate(d.date)"
      >
        <span class="day-name">{{ d.dayName }}</span>
        <span class="day-num">{{ d.dayNum }}</span>
      </div>
    </div>

    <!-- Todo list -->
    <div class="todo-section">
      <div class="todo-header">{{ dayjs(selectedDate).format('M月D日') }} 待办</div>
      <div v-for="todo in filteredTodos" :key="todo.id" class="todo-item">
        <van-checkbox :model-value="todo.done" @click="toggleTodo(todo)" shape="square" />
        <span class="todo-text" :class="{ done: todo.done }">{{ todo.content }}</span>
      </div>
      <div v-if="filteredTodos.length === 0" class="empty">暂无待办</div>
    </div>

    <!-- Quick add -->
    <div class="add-bar">
      <van-field
        v-model="newContent"
        placeholder="添加待办..."
        @keyup.enter="addTodo"
        clearable
      />
      <van-button type="primary" size="small" @click="addTodo">添加</van-button>
    </div>

    <!-- Calendar popup -->
    <van-calendar
      v-model:show="showCalendar"
      @confirm="onCalendarConfirm"
      :min-date="dayjs().subtract(1, 'year').toDate()"
      :max-date="dayjs().add(1, 'year').toDate()"
    />
  </div>
</template>

<style scoped>
.schedule-view { padding-bottom: 16px; }
.date-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.date-display { font-size: 18px; font-weight: 600; color: #3D3D35; }
.week-bar {
  display: flex; gap: 4px; margin-bottom: 20px;
  background: #fff; border-radius: 10px; padding: 8px;
}
.week-day {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; padding: 6px 0;
  border-radius: 8px; cursor: pointer;
  transition: background 0.15s;
}
.week-day:hover { background: #f0f0e8; }
.week-day.active { background: #5A6B4A; color: #fff; }
.week-day.today .day-num { font-weight: 700; }
.day-name { font-size: 11px; margin-bottom: 2px; }
.day-num { font-size: 15px; }
.todo-section { margin-bottom: 12px; }
.todo-header { font-size: 14px; font-weight: 600; color: #3D3D35; margin-bottom: 8px; }
.todo-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 12px; background: #fff; border-radius: 8px; margin-bottom: 4px;
}
.todo-text { font-size: 14px; }
.todo-text.done { text-decoration: line-through; color: #B5B5A5; }
.empty { text-align: center; padding: 24px; color: #B5B5A5; font-size: 13px; }
.add-bar {
  display: flex; gap: 8px; align-items: center;
  background: #fff; border-radius: 10px; padding: 4px 8px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/mobile/views/ScheduleView.vue
git commit -m "feat: schedule view with week bar, calendar popup, todo list"
```

---

### Task 5: CourseView — Week Grid + Edit/Delete

**Files:**
- Create: `src/mobile/components/CourseEditPopup.vue`
- Create: `src/mobile/views/CourseView.vue`

- [ ] **Step 1: Create CourseEditPopup**

```vue
<!-- src/mobile/components/CourseEditPopup.vue -->
<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Course, Schedule } from '@/types'
import { COLORS } from '@/types'

const props = defineProps<{
  show: boolean
  course: Course | null
}>()

const emit = defineEmits<{
  close: []
  save: [data: Omit<Course, 'id'>]
  delete: []
}>()

const form = ref({
  name: '',
  teacher: '',
  location: '',
  type: 'required' as 'required' | 'elective',
  color: COLORS[0],
  schedules: [{ dayOfWeek: 1, startSlot: 1, duration: 2, weekType: 'all' as const }] as Schedule[],
})

watch(() => props.show, (v) => {
  if (v && props.course) {
    form.value = {
      name: props.course.name,
      teacher: props.course.teacher,
      location: props.course.location,
      type: props.course.type,
      color: props.course.color,
      schedules: props.course.schedules?.length ? [...props.course.schedules] : form.value.schedules,
    }
  } else if (v) {
    form.value = {
      name: '', teacher: '', location: '',
      type: 'required', color: COLORS[0],
      schedules: [{ dayOfWeek: 1, startSlot: 1, duration: 2, weekType: 'all' }],
    }
  }
})

function addSchedule() {
  form.value.schedules.push({ dayOfWeek: 1, startSlot: 1, duration: 2, weekType: 'all' })
}

function removeSchedule(idx: number) {
  if (form.value.schedules.length > 1) form.value.schedules.splice(idx, 1)
}

function handleSave() {
  if (!form.value.name.trim()) return
  emit('save', { ...form.value, name: form.value.name.trim() })
}

function dayLabel(d: number): string {
  return ['周一','周二','周三','周四','周五','周六','周日'][d - 1] || ''
}
</script>

<template>
  <van-popup :show="show" position="bottom" round @close="emit('close')" style="max-height: 85vh">
    <div class="popup-content">
      <div class="popup-header">
        <span>{{ course ? '编辑课程' : '添加课程' }}</span>
        <van-button text @click="emit('close')">取消</van-button>
      </div>

      <van-form @submit="handleSave">
        <van-field v-model="form.name" label="课程名" placeholder="课程名" required />
        <van-field v-model="form.teacher" label="教师" placeholder="教师" />
        <van-field v-model="form.location" label="教室" placeholder="教室" />
        <van-field label="颜色">
          <template #input>
            <div class="color-picker">
              <div
                v-for="c in COLORS" :key="c"
                class="color-dot"
                :class="{ active: form.color === c }"
                :style="{ background: c }"
                @click="form.color = c"
              />
            </div>
          </template>
        </van-field>

        <div class="schedules-section">
          <div class="schedules-header">
            <span>上课时间</span>
            <van-button size="small" plain type="primary" @click="addSchedule">添加时段</van-button>
          </div>
          <div v-for="(s, i) in form.schedules" :key="i" class="schedule-row">
            <van-field v-model="s.dayOfWeek" label="星期" type="digit" placeholder="1-7" />
            <van-field v-model="s.startSlot" label="开始节次" type="digit" placeholder="1-12" />
            <van-field v-model="s.duration" label="持续节数" type="digit" placeholder="1-4" />
            <van-field label="周次">
              <template #input>
                <van-radio-group v-model="s.weekType" direction="horizontal">
                  <van-radio name="all">全</van-radio>
                  <van-radio name="odd">单</van-radio>
                  <van-radio name="even">双</van-radio>
                </van-radio-group>
              </template>
            </van-field>
            <van-button v-if="form.schedules.length > 1" icon="delete" text type="danger" size="small" @click="removeSchedule(i)" />
          </div>
        </div>

        <div style="margin: 16px; display: flex; gap: 8px;">
          <van-button v-if="course" round block type="danger" @click="emit('delete')">删除此课程</van-button>
          <van-button round block type="primary" native-type="submit">保存</van-button>
        </div>
      </van-form>
    </div>
  </van-popup>
</template>

<style scoped>
.popup-content { padding: 16px; }
.popup-header {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 16px; font-weight: 600; margin-bottom: 12px; padding: 0 4px;
}
.color-picker { display: flex; gap: 8px; flex-wrap: wrap; }
.color-dot {
  width: 24px; height: 24px; border-radius: 50%; cursor: pointer;
  border: 2px solid transparent; transition: border-color 0.15s;
}
.color-dot.active { border-color: #3D3D35; }
.schedules-section { margin-top: 8px; }
.schedules-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 16px; font-size: 14px; font-weight: 500;
}
.schedule-row {
  border: 1px solid #eee; border-radius: 8px; margin: 8px 16px; padding: 4px 0;
}
</style>
```

- [ ] **Step 2: Create CourseView**

```vue
<!-- src/mobile/views/CourseView.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useCourseStore } from '@/stores/course'
import { useTimeSlotStore } from '@/stores/timeSlot'
import { useSemesterStore } from '@/stores/semester'
import type { Course } from '@/types'
import CourseEditPopup from '@/mobile/components/CourseEditPopup.vue'

const courseStore = useCourseStore()
const timeSlotStore = useTimeSlotStore()
const semesterStore = useSemesterStore()

const showPopup = ref(false)
const editingCourse = ref<Course | null>(null)

const days = ['周一','周二','周三','周四','周五','周六','周日']

const timeRows = computed(() =>
  timeSlotStore.timeSlots
    .sort((a, b) => a.slot - b.slot)
    .map(ts => ({
      slot: ts.slot,
      time: `${ts.startTime}`,
    }))
)

function getCoursesForDay(dayIndex: number) {
  const week = semesterStore.currentWeek
  return courseStore.courses.filter(c =>
    c.schedules?.some(s => s.dayOfWeek === dayIndex + 1 && courseStore.isScheduleVisible(s, week))
  ).map(c => {
    const sched = c.schedules!.find(s => s.dayOfWeek === dayIndex + 1 && courseStore.isScheduleVisible(s, week))
    const slot = timeSlotStore.timeSlots.find(ts => ts.slot === sched?.startSlot)
    return {
      ...c,
      _startSlot: sched?.startSlot ?? 1,
      _duration: sched?.duration ?? 1,
      _time: slot ? slot.startTime : '',
    }
  }).sort((a, b) => a._startSlot - b._startSlot)
}

function openAdd() {
  editingCourse.value = null
  showPopup.value = true
}

function openEdit(course: any) {
  editingCourse.value = course
  showPopup.value = true
}

async function handleSave(data: any) {
  if (editingCourse.value?.id) {
    await courseStore.updateCourse(editingCourse.value.id, data)
  } else {
    await courseStore.addCourse(data)
  }
  showPopup.value = false
}

async function handleDelete() {
  if (editingCourse.value?.id) {
    await courseStore.deleteCourse(editingCourse.value.id)
  }
  showPopup.value = false
}
</script>

<template>
  <div class="course-view">
    <!-- Week nav -->
    <div class="week-nav">
      <van-button icon="arrow-left" plain size="small" @click="semesterStore.prevWeek()" />
      <span class="week-label">第 {{ semesterStore.currentWeek }} 周</span>
      <van-button icon="arrow" plain size="small" @click="semesterStore.nextWeek()" />
      <van-button size="small" plain type="primary" style="margin-left:auto" @click="openAdd">添加</van-button>
    </div>

    <!-- Grid -->
    <div class="grid-wrapper">
      <div class="grid">
        <div class="grid-header">
          <div class="time-col"></div>
          <div v-for="day in days" :key="day" class="day-header">{{ day }}</div>
        </div>
        <div class="grid-body">
          <div v-for="tr in timeRows" :key="tr.slot" class="grid-row">
            <div class="time-col time-cell">{{ tr.time }}</div>
            <div v-for="(_, di) in 7" :key="di" class="cell" @click="openAdd()">
              <div
                v-for="c in getCoursesForDay(di).filter(c => c._startSlot === tr.slot)"
                :key="c.id"
                class="course-chip"
                :style="{ background: c.color + '22', borderLeftColor: c.color }"
                @click.stop="openEdit(c)"
              >
                <span class="chip-name">{{ c.name }}</span>
                <span class="chip-location">{{ c.location }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <CourseEditPopup
      :show="showPopup"
      :course="editingCourse"
      @close="showPopup = false"
      @save="handleSave"
      @delete="handleDelete"
    />
  </div>
</template>

<style scoped>
.course-view { padding-bottom: 16px; }
.week-nav {
  display: flex; align-items: center; gap: 8px; margin-bottom: 12px;
}
.week-label { font-size: 16px; font-weight: 600; color: #3D3D35; }
.grid-wrapper { overflow-x: auto; }
.grid { min-width: 400px; }
.grid-header { display: flex; }
.day-header {
  flex: 1; text-align: center; font-size: 12px; font-weight: 600;
  color: #5A6B4A; padding: 6px 0; border-bottom: 1px solid #eee;
}
.time-col { width: 44px; flex-shrink: 0; }
.grid-row { display: flex; border-bottom: 1px solid #f0f0e8; }
.time-cell {
  font-size: 11px; color: #8A8A7A; padding: 4px 4px 4px 0;
  text-align: right; line-height: 32px;
}
.cell {
  flex: 1; min-height: 34px; padding: 1px;
  border-left: 1px solid #f0f0e8; cursor: pointer;
}
.course-chip {
  border-left: 3px solid; border-radius: 4px;
  padding: 2px 4px; margin-bottom: 1px; cursor: pointer;
}
.chip-name { font-size: 11px; font-weight: 500; display: block; }
.chip-location { font-size: 10px; color: #8A8A7A; }
</style>
```

- [ ] **Step 3: Commit**

```bash
git add src/mobile/views/CourseView.vue src/mobile/components/CourseEditPopup.vue
git commit -m "feat: course view with week grid and edit popup"
```

---

### Task 6: CsgoView — Grouped Event List

**Files:**
- Create: `src/mobile/views/CsgoView.vue`

- [ ] **Step 1: Create CsgoView**

```vue
<!-- src/mobile/views/CsgoView.vue -->
<script setup lang="ts">
import { computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useCsgoStore } from '@/stores/csgo'

const csgoStore = useCsgoStore()

onMounted(() => {
  if (csgoStore.events.length === 0) csgoStore.refresh()
})

const liveEvents = computed(() =>
  csgoStore.events.filter(e => {
    const now = new Date()
    const start = e.startDate ? new Date(e.startDate) : null
    const end = e.endDate ? new Date(e.endDate) : null
    return start && end && start <= now && end >= now
  })
)

const upcomingEvents = computed(() =>
  csgoStore.events
    .filter(e => {
      const start = e.startDate ? new Date(e.startDate) : null
      return start && start > new Date()
    })
    .sort((a, b) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime())
)

const pastEvents = computed(() =>
  csgoStore.events
    .filter(e => {
      const end = e.endDate ? new Date(e.endDate) : null
      return end && end < new Date()
    })
    .sort((a, b) => new Date(b.startDate).getTime() - new Date(a.startDate).getTime())
)

function formatDateRange(e: { startDate: string; endDate: string }): string {
  const s = dayjs(e.startDate).format('M/D')
  const end = dayjs(e.endDate).format('M/D')
  return s === end ? s : `${s} - ${end}`
}

function formatMatchTime(e: { beginAt: string }): string {
  if (!e.beginAt) return ''
  return dayjs(e.beginAt).format('M/D HH:mm')
}
</script>

<template>
  <div class="csgo-view">
    <!-- LIVE -->
    <div v-if="liveEvents.length" class="group">
      <div class="group-title live-title">
        <span class="live-dot" /> LIVE
      </div>
      <div v-for="e in liveEvents" :key="e.id" class="event-card live-card">
        <div class="event-name">{{ e.matchName || e.leagueName }}</div>
        <div class="event-teams">{{ e.team1 }} vs {{ e.team2 }}</div>
        <div class="event-time">{{ formatMatchTime(e) }}</div>
      </div>
    </div>

    <!-- Upcoming -->
    <div class="group">
      <div class="group-title">即将开始</div>
      <div v-for="e in upcomingEvents" :key="e.id" class="event-card">
        <div class="event-name">{{ e.matchName || e.leagueName }}</div>
        <div class="event-teams">{{ e.team1 }} vs {{ e.team2 }}</div>
        <div class="event-time">{{ formatDateRange(e) }}</div>
      </div>
      <div v-if="upcomingEvents.length === 0" class="empty">暂无即将开始的赛事</div>
    </div>

    <!-- Past -->
    <div class="group">
      <div class="group-title">往期</div>
      <div v-for="e in pastEvents" :key="e.id" class="event-card past">
        <div class="event-name">{{ e.matchName || e.leagueName }}</div>
        <div class="event-teams">{{ e.team1 }} vs {{ e.team2 }}</div>
        <div class="event-time">{{ formatDateRange(e) }}</div>
      </div>
      <div v-if="pastEvents.length === 0" class="empty">暂无往期赛事</div>
    </div>
  </div>
</template>

<style scoped>
.csgo-view { padding-bottom: 16px; }
.group { margin-bottom: 20px; }
.group-title {
  font-size: 14px; font-weight: 600; color: #3D3D35;
  margin-bottom: 8px; display: flex; align-items: center; gap: 6px;
}
.live-title { color: #e74c3c; }
.live-dot {
  display: inline-block; width: 8px; height: 8px;
  border-radius: 50%; background: #e74c3c; animation: pulse 1.5s infinite;
}
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
.event-card {
  background: #fff; border-radius: 10px; padding: 12px 14px;
  margin-bottom: 6px;
}
.live-card { border-left: 3px solid #e74c3c; }
.event-name { font-size: 14px; font-weight: 500; color: #3D3D35; }
.event-teams { font-size: 13px; color: #5A6B4A; margin-top: 2px; }
.event-time { font-size: 12px; color: #8A8A7A; margin-top: 2px; }
.past { opacity: 0.5; }
.empty { text-align: center; padding: 20px; color: #B5B5A5; font-size: 13px; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/mobile/views/CsgoView.vue
git commit -m "feat: csgo view with live/upcoming/past groups"
```

---

### Task 7: SettingsView — Semester, City, Dark Mode, Logout

**Files:**
- Create: `src/mobile/views/SettingsView.vue`

- [ ] **Step 1: Create SettingsView**

```vue
<!-- src/mobile/views/SettingsView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useSemesterStore } from '@/stores/semester'
import { useWeatherStore } from '@/stores/weather'
import { useAuthStore } from '@/stores/auth'
import { doc, getDoc, setDoc } from 'firebase/firestore'
import { db } from '@/firebase/init'

const semesterStore = useSemesterStore()
const weatherStore = useWeatherStore()
const authStore = useAuthStore()

const darkMode = ref(false)

onMounted(async () => {
  if (authStore.user) {
    const snap = await getDoc(doc(db, 'users', authStore.user.uid))
    darkMode.value = !!snap.data()?.settings?.darkMode
  }
})

async function toggleDark(val: boolean) {
  darkMode.value = val
  document.documentElement.classList.toggle('dark', val)
  if (authStore.user) {
    await setDoc(doc(db, 'users', authStore.user.uid), {
      settings: { darkMode: val },
    }, { merge: true })
  }
}

function onCityChange() {
  weatherStore.refresh(true)
}

async function handleLogout() {
  await authStore.logout()
}
</script>

<template>
  <div class="settings-view">
    <van-cell-group inset>
      <van-cell title="学期设置" is-link @click="semesterStore.fetchSemester()">
        <template #value>
          <span class="cell-value">{{ semesterStore.semesterName || '未设置' }}</span>
        </template>
      </van-cell>
      <van-cell title="起始日期">
        <template #value>
          <span class="cell-value">{{ semesterStore.startDate ? dayjs(semesterStore.startDate).format('YYYY/M/D') : '未设置' }}</span>
        </template>
      </van-cell>
      <van-cell title="总周数">
        <template #value>
          <span class="cell-value">{{ semesterStore.totalWeeks }} 周</span>
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px;">
      <van-cell title="城市">
        <template #value>
          <van-field
            v-model="weatherStore.city"
            placeholder="输入城市名"
            size="small"
            style="width: 120px"
            @blur="onCityChange"
          />
        </template>
      </van-cell>
      <van-cell title="深色模式">
        <template #right-icon>
          <van-switch :model-value="darkMode" @update:model-value="toggleDark" active-color="#5A6B4A" />
        </template>
      </van-cell>
    </van-cell-group>

    <div style="margin: 32px 16px;">
      <van-button round block type="danger" plain @click="handleLogout">登出</van-button>
    </div>
  </div>
</template>

<style scoped>
.settings-view { padding-bottom: 16px; }
.cell-value { color: #8A8A7A; font-size: 13px; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/mobile/views/SettingsView.vue
git commit -m "feat: settings view with semester, city, dark mode, logout"
```

---

### Task 8: Integrate Mobile Views & Configure Capacitor

**Files:**
- Modify: `src/App.vue`

- [ ] **Step 1: Modify App.vue to detect Capacitor and redirect**

Add Capacitor detection and redirect logic to `src/App.vue`:

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { doc, getDoc } from 'firebase/firestore'
import { db } from '@/firebase/init'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTopBar from '@/components/layout/AppTopBar.vue'
import { isNativeApp } from '@/utils/platform'

const router = useRouter()
const auth = useAuthStore()

onMounted(async () => {
  // Dark mode init
  if (auth.user) {
    const snap = await getDoc(doc(db, 'users', auth.user.uid))
    if (snap.exists()) {
      const dark = !!snap.data()?.settings?.darkMode
      document.documentElement.classList.toggle('dark', dark)
    }
  }

  // Redirect native app to mobile routes
  if (isNativeApp() && auth.user && !window.location.pathname.startsWith('/m/')) {
    router.replace('/m/home')
  }
})
</script>
```

- [ ] **Step 2: Verify build**

```bash
npm run build
```

Expected: `vue-tsc -b && vite build` passes with 0 errors.

- [ ] **Step 3: Add Capacitor Android platform**

```bash
npx cap add android
npx cap copy
```

Expected: `android/` directory created with native Android project.

- [ ] **Step 4: Commit**

```bash
git add src/App.vue
git commit -m "feat: capacitor detection and native app redirect"
```

---

### Task 9: Final Self-Review

**Files:** None (review only)

- [ ] **Step 1: Verify all files exist**

Check that all 11 new files and 2 modified files are in place:
- `src/utils/platform.ts`
- `src/mobile/router.ts`
- `src/mobile/layout/MobileLayout.vue`
- `src/mobile/components/MobileNavBar.vue`
- `src/mobile/components/CourseEditPopup.vue`
- `src/mobile/views/HomeView.vue`
- `src/mobile/views/ScheduleView.vue`
- `src/mobile/views/CourseView.vue`
- `src/mobile/views/CsgoView.vue`
- `src/mobile/views/SettingsView.vue`
- `src/mobile/router.ts`
- `src/router/index.ts` (modified)
- `src/App.vue` (modified)

- [ ] **Step 2: Verify build passes**

```bash
npm run build
```

- [ ] **Step 3: Commit any final fixes**

```bash
git add -A
git commit -m "chore: finalize mobile app implementation"
```
