# 日常仪表盘应用 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a personal daily dashboard with weather, schedule, important dates, todo, CSGO events, and course schedule.

**Architecture:** Vue 3 + TypeScript SPA with Pinia stores, Firebase Auth + Firestore, Element Plus UI. External APIs: OpenWeatherMap (weather), PandaScore (CSGO). Course schedule module ported from existing class-table project.

**Location:** `d:\code\claude\daily-dashboard\`

**Reference project:** `d:\code\claude\class-table\` — copy patterns for Firebase init, auth store, router, layout, course components.

**Tech Stack:** Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router, Firebase, dayjs, OpenWeatherMap, PandaScore

---

## File Structure

```
daily-dashboard/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── env.d.ts
├── public/
│   └── csgo-events.json
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── firebase/
│   │   └── init.ts
│   ├── router/
│   │   └── index.ts
│   ├── types/
│   │   └── index.ts
│   ├── utils/
│   │   ├── firestore.ts
│   │   ├── weather.ts
│   │   └── pandascore.ts
│   ├── stores/
│   │   ├── auth.ts
│   │   ├── calendar.ts
│   │   ├── todo.ts
│   │   ├── importantDates.ts
│   │   ├── csgo.ts
│   │   ├── weather.ts
│   │   ├── course.ts
│   │   ├── semester.ts
│   │   └── timeSlot.ts
│   ├── views/
│   │   ├── LoginView.vue
│   │   ├── RegisterView.vue
│   │   ├── NotFoundView.vue
│   │   ├── DashboardView.vue
│   │   ├── CalendarView.vue
│   │   ├── TodoView.vue
│   │   ├── ImportantDatesView.vue
│   │   ├── CourseGridView.vue
│   │   └── SettingsView.vue
│   └── components/
│       ├── layout/
│       │   ├── AppSidebar.vue
│       │   └── AppTopBar.vue
│       ├── widgets/
│       │   ├── WeatherWidget.vue
│       │   ├── ScheduleWidget.vue
│       │   ├── ImportantDatesWidget.vue
│       │   ├── TodoWidget.vue
│       │   └── CsgoWidget.vue
│       └── course/
│           ├── CourseGrid.vue
│           ├── CourseCard.vue
│           ├── GridHeader.vue
│           ├── TimeColumn.vue
│           ├── CourseDialog.vue
│           └── TimeSlotEditor.vue
```

---

### Phase 1 — 项目骨架

### Task 1: 脚手架项目

**Files:**
- Create: `daily-dashboard/package.json`
- Create: `daily-dashboard/index.html`
- Create: `daily-dashboard/vite.config.ts`
- Create: `daily-dashboard/tsconfig.json`
- Create: `daily-dashboard/tsconfig.app.json`
- Create: `daily-dashboard/tsconfig.node.json`
- Create: `daily-dashboard/env.d.ts`
- Create: `daily-dashboard/src/main.ts`
- Create: `daily-dashboard/src/App.vue`

- [ ] **Step 1: Create project root config files**

Create `daily-dashboard/package.json` matching the class-table project's dependencies:

```json
{
  "name": "daily-dashboard",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc -b && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.5.32",
    "vue-router": "^4.6.4",
    "pinia": "^3.0.4",
    "element-plus": "^2.13.7",
    "@element-plus/icons-vue": "^2.3.2",
    "firebase": "^12.12.1",
    "dayjs": "^1.11.20"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^6.0.6",
    "typescript": "~6.0.2",
    "vite": "^8.0.10",
    "vue-tsc": "^3.2.7",
    "@types/node": "^24.12.2",
    "@vue/tsconfig": "^0.9.1"
  }
}
```

Create `daily-dashboard/index.html`:

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/favicon.ico" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>日常仪表盘</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

Create `daily-dashboard/vite.config.ts`:

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
})
```

Create `daily-dashboard/tsconfig.json`:

```json
{
  "files": [],
  "references": [
    { "path": "./tsconfig.app.json" },
    { "path": "./tsconfig.node.json" }
  ]
}
```

Create `daily-dashboard/tsconfig.app.json`:

```json
{
  "extends": "@vue/tsconfig/tsconfig.dom.json",
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "preserve",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "forceConsistentCasingInFileNames": true,
    "skipLibCheck": true,
    "noEmit": true,
    "paths": {
      "@/*": ["./src/*"]
    },
    "baseUrl": "."
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue", "env.d.ts"]
}
```

Create `daily-dashboard/tsconfig.node.json`:

```json
{
  "extends": "@vue/tsconfig/tsconfig.node.json",
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2023"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "noEmit": true,
    "strict": true
  },
  "include": ["vite.config.ts"]
}
```

Create `daily-dashboard/env.d.ts`:

```ts
/// <reference types="vite/client" />
```

- [ ] **Step 2: Create src/main.ts**

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

const auth = useAuthStore()
auth.init()

app.mount('#app')
```

- [ ] **Step 3: Create src/App.vue**

```vue
<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

if (!auth.isAuthenticated()) {
  router.push('/login')
}
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>
```

- [ ] **Step 4: Create .env file**

Create `daily-dashboard/.env`:

```
VITE_FIREBASE_API_KEY=your-api-key
VITE_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your-project
VITE_FIREBASE_STORAGE_BUCKET=your-project.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=your-sender-id
VITE_FIREBASE_APP_ID=your-app-id
VITE_OPENWEATHERMAP_API_KEY=your-weather-api-key
VITE_PANDASCORE_API_KEY=your-pandascore-api-key
```

(Note: user fills in actual values)

- [ ] **Step 5: Install dependencies**

Run: `cd d:\code\claude\daily-dashboard && npm install`
Expected: all packages installed without errors

- [ ] **Step 6: Test dev server starts**

Run: `cd d:\code\claude\daily-dashboard && npx vite --port 5174` (run in background, verify it starts)

- [ ] **Step 7: Commit**

```bash
git add .
git commit -m "feat: scaffold daily-dashboard project with Vite + Vue 3 + TS"
```

---

### Task 2: Firebase 初始化

**Files:**
- Create: `daily-dashboard/src/firebase/init.ts`
- Create: `daily-dashboard/src/stores/auth.ts`

- [ ] **Step 1: Create Firebase init file**

`src/firebase/init.ts` — copy from class-table project:

```ts
import { initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
export const db = getFirestore(app)
```

- [ ] **Step 2: Create auth store**

`src/stores/auth.ts` — copy from class-table project with adjusted app name:

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signInWithPopup,
  GoogleAuthProvider,
  signOut,
  onAuthStateChanged,
  sendPasswordResetEmail,
  type User,
} from 'firebase/auth'
import { auth, db } from '@/firebase/init'
import { doc, setDoc, getDoc } from 'firebase/firestore'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const loading = ref(true)

  const isAuthenticated = () => !!user.value

  function init() {
    onAuthStateChanged(auth, (u) => {
      user.value = u
      loading.value = false
    })
  }

  async function login(email: string, password: string) {
    return signInWithEmailAndPassword(auth, email, password)
  }

  async function register(email: string, password: string, name: string) {
    const cred = await createUserWithEmailAndPassword(auth, email, password)
    const uid = cred.user.uid
    await setDoc(doc(db, 'users', uid), {
      profile: { name, email, photoURL: '' },
      settings: { themeColor: '#409EFF', darkMode: false, weekStartsOn: 1 },
    })
    return cred
  }

  async function loginWithGoogle() {
    const provider = new GoogleAuthProvider()
    const cred = await signInWithPopup(auth, provider)
    const uid = cred.user.uid
    const snap = await getDoc(doc(db, 'users', uid))
    if (!snap.exists()) {
      await setDoc(doc(db, 'users', uid), {
        profile: { name: cred.user.displayName || '', email: cred.user.email, photoURL: cred.user.photoURL || '' },
        settings: { themeColor: '#409EFF', darkMode: false, weekStartsOn: 1 },
      })
    }
    return cred
  }

  async function resetPassword(email: string) {
    return sendPasswordResetEmail(auth, email)
  }

  async function logout() {
    return signOut(auth)
  }

  return { user, loading, isAuthenticated, init, login, register, loginWithGoogle, resetPassword, logout }
})
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "feat: add Firebase init and auth store"
```

---

### Task 3: 路由配置 + 守卫

**Files:**
- Create: `daily-dashboard/src/router/index.ts`

- [ ] **Step 1: Create router**

```ts
import { createRouter, createWebHistory } from 'vue-router'
import { getAuth } from 'firebase/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
        },
        {
          path: 'calendar',
          name: 'Calendar',
          component: () => import('@/views/CalendarView.vue'),
        },
        {
          path: 'todo',
          name: 'Todo',
          component: () => import('@/views/TodoView.vue'),
        },
        {
          path: 'dates',
          name: 'ImportantDates',
          component: () => import('@/views/ImportantDatesView.vue'),
        },
        {
          path: 'course',
          name: 'Course',
          component: () => import('@/views/CourseGridView.vue'),
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/SettingsView.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFoundView.vue'),
    },
  ],
})

function getCurrentUser() {
  const auth = getAuth()
  return auth.authStateReady().then(() => auth.currentUser)
}

router.beforeEach(async (to, _from, next) => {
  const user = await getCurrentUser()
  if (to.meta.requiresAuth && !user) {
    next('/login')
  } else if (to.meta.guest && user) {
    next('/')
  } else {
    next()
  }
})

export default router
```

Note: The auth routes are top-level (no sidebar layout), while all authenticated routes share a parent route that will render layout components in the view.

- [ ] **Step 2: Commit**

```bash
git add .
git commit -m "feat: add router with auth guards"
```

---

### Task 4: 布局组件 (Sidebar + TopBar)

**Files:**
- Create: `daily-dashboard/src/components/layout/AppSidebar.vue`
- Create: `daily-dashboard/src/components/layout/AppTopBar.vue`
- Modify: `daily-dashboard/src/App.vue` (add layout wrapper)

- [ ] **Step 1: Create AppSidebar.vue**

Sidebar with navigation items for all modules. Copy the collapse pattern from class-table.

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Fold, Expand,
  HomeFilled,
  Calendar,
  List,
  StarFilled,
  Reading,
  Setting,
} from '@element-plus/icons-vue'

const collapsed = ref(false)

function toggleCollapse() {
  collapsed.value = !collapsed.value
  document.documentElement.style.setProperty(
    '--sidebar-width',
    collapsed.value ? '64px' : '220px'
  )
}
</script>

<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="sidebar-header">
      <h2 v-show="!collapsed" class="app-title">仪表盘</h2>
      <el-button
        :icon="collapsed ? Expand : Fold"
        text
        class="collapse-btn"
        @click="toggleCollapse"
      />
    </div>
    <nav class="sidebar-nav">
      <router-link to="/" class="nav-item" :title="'仪表盘'">
        <el-icon><HomeFilled /></el-icon>
        <span v-show="!collapsed">仪表盘</span>
      </router-link>
      <router-link to="/calendar" class="nav-item" :title="'日程'">
        <el-icon><Calendar /></el-icon>
        <span v-show="!collapsed">日程</span>
      </router-link>
      <router-link to="/todo" class="nav-item" :title="'待办'">
        <el-icon><List /></el-icon>
        <span v-show="!collapsed">待办</span>
      </router-link>
      <router-link to="/dates" class="nav-item" :title="'重大日期'">
        <el-icon><StarFilled /></el-icon>
        <span v-show="!collapsed">重大日期</span>
      </router-link>
      <router-link to="/course" class="nav-item" :title="'课表'">
        <el-icon><Reading /></el-icon>
        <span v-show="!collapsed">课表</span>
      </router-link>
      <router-link to="/settings" class="nav-item" :title="'设置'">
        <el-icon><Setting /></el-icon>
        <span v-show="!collapsed">设置</span>
      </router-link>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width, 220px);
  height: 100vh;
  background: var(--el-bg-color);
  box-shadow: 2px 0 12px rgba(139, 129, 120, 0.08);
  display: flex;
  flex-direction: column;
  padding: 16px;
  position: fixed;
  left: 0;
  top: 0;
  transition: width 0.25s ease;
  overflow: hidden;
  z-index: 100;
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  min-height: 24px;
}
.sidebar.collapsed .sidebar-header {
  justify-content: center;
}
.collapse-btn {
  font-size: 18px;
}
.app-title {
  font-family: "Noto Serif SC", "Songti SC", "STSong", serif;
  font-size: 20px;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  color: var(--el-text-color-regular);
  font-size: 14px;
  transition: all 0.2s;
  white-space: nowrap;
  text-decoration: none;
}
.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 10px 0;
}
.nav-item:hover,
.nav-item.router-link-exact-active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
</style>
```

- [ ] **Step 2: Create AppTopBar.vue**

```vue
<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <header class="topbar">
    <div class="topbar-left">
      <slot name="left" />
    </div>
    <div class="topbar-right">
      <el-dropdown v-if="auth.user" trigger="click">
        <span class="user-info">
          {{ auth.user.email }}
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="auth.logout()">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
}
.user-info {
  cursor: pointer;
  font-size: 14px;
  color: var(--el-text-color-regular);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
```

- [ ] **Step 3: Update App.vue to use layout**

Rewrite `src/App.vue` to use layout for authenticated routes:

```vue
<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTopBar from '@/components/layout/AppTopBar.vue'

const auth = useAuthStore()
</script>

<template>
  <!-- Auth pages (login/register) render directly without layout -->
  <template v-if="auth.user">
    <AppSidebar />
    <div class="main-area">
      <AppTopBar />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </template>
  <template v-else-if="!auth.loading">
    <router-view />
  </template>
</template>

<style>
:root {
  --sidebar-width: 220px;
}
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
.main-area {
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.25s ease;
}
.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--el-bg-color-page);
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "feat: add sidebar and topbar layout components"
```

---

### Task 5: Auth Views (Login + Register + NotFound)

**Files:**
- Create: `daily-dashboard/src/views/LoginView.vue`
- Create: `daily-dashboard/src/views/RegisterView.vue`
- Create: `daily-dashboard/src/views/NotFoundView.vue`

- [ ] **Step 1: Create LoginView.vue**

Copy from class-table project, change app title to "仪表盘":

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const email = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    router.push('/')
  } catch (e: any) {
    const msg = e.code === 'auth/user-not-found' || e.code === 'auth/wrong-password'
      ? '邮箱或密码错误'
      : e.code === 'auth/invalid-credential'
        ? '邮箱或密码错误'
        : '登录失败，请重试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

async function handleGoogleLogin() {
  loading.value = true
  try {
    await auth.loginWithGoogle()
    router.push('/')
  } catch {
    ElMessage.error('Google 登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">日常仪表盘</h1>
      <el-form @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="email" placeholder="邮箱" type="email" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" placeholder="密码" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="w-full" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <el-divider>或</el-divider>
      <el-button size="large" class="w-full" @click="handleGoogleLogin" :disabled="loading">
        Google 登录
      </el-button>
      <div class="auth-links">
        <router-link to="/register">注册账号</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
}
.auth-card {
  width: 400px;
  padding: 40px;
  background: var(--el-bg-color-overlay);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(139, 129, 120, 0.10);
}
.auth-title {
  text-align: center;
  margin-bottom: 32px;
  font-size: 28px;
  color: var(--el-text-color-primary);
}
.w-full {
  width: 100%;
}
.auth-links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 14px;
}
.auth-links a {
  color: var(--el-color-primary);
}
</style>
```

- [ ] **Step 2: Create RegisterView.vue**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const name = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)

async function handleRegister() {
  loading.value = true
  try {
    await auth.register(email.value, password.value, name.value)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e: any) {
    const msg = e.code === 'auth/email-already-in-use'
      ? '该邮箱已被注册'
      : '注册失败，请重试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">注册账号</h1>
      <el-form @submit.prevent="handleRegister">
        <el-form-item>
          <el-input v-model="name" placeholder="姓名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="email" placeholder="邮箱" type="email" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" placeholder="密码" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="w-full" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-links">
        <router-link to="/login">已有账号？登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
}
.auth-card {
  width: 400px;
  padding: 40px;
  background: var(--el-bg-color-overlay);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(139, 129, 120, 0.10);
}
.auth-title {
  text-align: center;
  margin-bottom: 32px;
  font-size: 28px;
  color: var(--el-text-color-primary);
}
.w-full { width: 100%; }
.auth-links {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}
.auth-links a { color: var(--el-color-primary); }
</style>
```

- [ ] **Step 3: Create NotFoundView.vue**

```vue
<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--el-text-color-secondary);
}
.not-found h1 {
  font-size: 72px;
  color: var(--el-text-color-primary);
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "feat: add login, register, and 404 views"
```

---

### Phase 2 — 核心功能

### Task 6: 类型定义 + Firestore 工具

**Files:**
- Create: `daily-dashboard/src/types/index.ts`
- Create: `daily-dashboard/src/utils/firestore.ts`

- [ ] **Step 1: Create types file**

```ts
export interface Semester {
  name: string
  startDate: Date
  totalWeeks: number
}

export interface Schedule {
  dayOfWeek: number
  startSlot: number
  duration: number
  weekType: 'all' | 'odd' | 'even' | 'custom'
  customWeeks?: number[]
}

export type CourseType = 'required' | 'elective'

export interface Course {
  id?: string
  name: string
  teacher: string
  location: string
  type: CourseType
  color: string
  schedules: Schedule[]
}

export interface TimeSlot {
  id?: string
  slot: number
  startTime: string
  endTime: string
}

export interface CalendarEvent {
  id?: string
  title: string
  date: Date
  time?: string
  note?: string
}

export interface TodoItem {
  id?: string
  content: string
  done: boolean
  dueDate?: Date
  createdAt: Date
}

export interface ImportantDate {
  id?: string
  name: string
  date: Date
  color: string
  type: 'birthday' | 'anniversary' | 'custom'
  note?: string
}

export interface CsgoEvent {
  id: string
  name: string
  startDate: string
  endDate: string
  logo?: string
}

export interface UserProfile {
  name: string
  email: string
  photoURL: string
}

export interface UserSettings {
  themeColor: string
  darkMode: boolean
  weekStartsOn: number
}

export const DEFAULT_TIME_SLOTS: Omit<TimeSlot, 'id'>[] = [
  { slot: 1, startTime: '08:00', endTime: '08:45' },
  { slot: 2, startTime: '08:50', endTime: '09:35' },
  { slot: 3, startTime: '09:50', endTime: '10:35' },
  { slot: 4, startTime: '10:40', endTime: '11:25' },
  { slot: 5, startTime: '11:30', endTime: '12:15' },
  { slot: 6, startTime: '14:00', endTime: '14:45' },
  { slot: 7, startTime: '14:50', endTime: '15:35' },
  { slot: 8, startTime: '15:50', endTime: '16:35' },
  { slot: 9, startTime: '16:40', endTime: '17:25' },
  { slot: 10, startTime: '19:00', endTime: '19:45' },
  { slot: 11, startTime: '19:50', endTime: '20:35' },
  { slot: 12, startTime: '20:40', endTime: '21:25' },
]

export const COLORS = [
  '#8FA88F', '#B5A88A', '#D4A76A', '#C47A6B',
  '#A88A9B', '#7A9B8A', '#C4A882', '#D4A0A0',
]
```

- [ ] **Step 2: Create firestore utility**

```ts
import { db } from '@/firebase/init'
import {
  doc, collection, addDoc, updateDoc, deleteDoc,
  onSnapshot, query, orderBy, where,
  type Unsubscribe,
} from 'firebase/firestore'
import type { Course, TimeSlot, CalendarEvent, TodoItem, ImportantDate } from '@/types'

function userPrefix(uid: string) {
  return `users/${uid}`
}

// Courses
export function coursesRef(uid: string) {
  return collection(db, userPrefix(uid), 'courses')
}

export function subscribeCourses(uid: string, callback: (courses: Course[]) => void): Unsubscribe {
  const q = query(coursesRef(uid), orderBy('name'))
  return onSnapshot(q, (snapshot) => {
    const list: Course[] = []
    snapshot.forEach((d) => {
      const data = d.data() as Omit<Course, 'id'>
      list.push({ id: d.id, ...data })
    })
    callback(list)
  })
}

export async function addCourse(uid: string, course: Omit<Course, 'id'>) {
  return addDoc(coursesRef(uid), course)
}

export async function updateCourse(uid: string, courseId: string, data: Partial<Course>) {
  return updateDoc(doc(db, userPrefix(uid), 'courses', courseId), data)
}

export async function deleteCourse(uid: string, courseId: string) {
  return deleteDoc(doc(db, userPrefix(uid), 'courses', courseId))
}

// TimeSlots
export function timeSlotsRef(uid: string) {
  return collection(db, userPrefix(uid), 'timeSlots')
}

export function subscribeTimeSlots(uid: string, callback: (slots: TimeSlot[]) => void): Unsubscribe {
  const q = query(timeSlotsRef(uid), orderBy('slot'))
  return onSnapshot(q, (snapshot) => {
    const list: TimeSlot[] = []
    snapshot.forEach((d) => {
      const data = d.data() as Omit<TimeSlot, 'id'>
      list.push({ id: d.id, ...data })
    })
    callback(list)
  })
}

export async function batchUpdateTimeSlots(uid: string, slots: Omit<TimeSlot, 'id'>[]) {
  const ref = timeSlotsRef(uid)
  for (const s of slots) {
    await addDoc(ref, s)
  }
}

// Calendar Events
export function calendarRef(uid: string) {
  return collection(db, userPrefix(uid), 'calendar')
}

export function subscribeCalendar(uid: string, callback: (events: CalendarEvent[]) => void): Unsubscribe {
  const q = query(calendarRef(uid), orderBy('date'))
  return onSnapshot(q, (snapshot) => {
    const list: CalendarEvent[] = []
    snapshot.forEach((d) => {
      const data = d.data() as Omit<CalendarEvent, 'id'>
      list.push({ id: d.id, ...data })
    })
    callback(list)
  })
}

export async function addCalendarEvent(uid: string, event: Omit<CalendarEvent, 'id'>) {
  return addDoc(calendarRef(uid), event)
}

export async function updateCalendarEvent(uid: string, eventId: string, data: Partial<CalendarEvent>) {
  return updateDoc(doc(db, userPrefix(uid), 'calendar', eventId), data)
}

export async function deleteCalendarEvent(uid: string, eventId: string) {
  return deleteDoc(doc(db, userPrefix(uid), 'calendar', eventId))
}

// Todos
export function todosRef(uid: string) {
  return collection(db, userPrefix(uid), 'todos')
}

export function subscribeTodos(uid: string, callback: (todos: TodoItem[]) => void): Unsubscribe {
  const q = query(todosRef(uid), orderBy('createdAt', 'desc'))
  return onSnapshot(q, (snapshot) => {
    const list: TodoItem[] = []
    snapshot.forEach((d) => {
      const data = d.data() as Omit<TodoItem, 'id'>
      list.push({ id: d.id, ...data })
    })
    callback(list)
  })
}

export async function addTodo(uid: string, todo: Omit<TodoItem, 'id'>) {
  return addDoc(todosRef(uid), todo)
}

export async function updateTodo(uid: string, todoId: string, data: Partial<TodoItem>) {
  return updateDoc(doc(db, userPrefix(uid), 'todos', todoId), data)
}

export async function deleteTodo(uid: string, todoId: string) {
  return deleteDoc(doc(db, userPrefix(uid), 'todos', todoId))
}

// Important Dates
export function importantDatesRef(uid: string) {
  return collection(db, userPrefix(uid), 'importantDates')
}

export function subscribeImportantDates(uid: string, callback: (dates: ImportantDate[]) => void): Unsubscribe {
  const q = query(importantDatesRef(uid), orderBy('date'))
  return onSnapshot(q, (snapshot) => {
    const list: ImportantDate[] = []
    snapshot.forEach((d) => {
      const data = d.data() as Omit<ImportantDate, 'id'>
      list.push({ id: d.id, ...data })
    })
    callback(list)
  })
}

export async function addImportantDate(uid: string, date: Omit<ImportantDate, 'id'>) {
  return addDoc(importantDatesRef(uid), date)
}

export async function updateImportantDate(uid: string, dateId: string, data: Partial<ImportantDate>) {
  return updateDoc(doc(db, userPrefix(uid), 'importantDates', dateId), data)
}

export async function deleteImportantDate(uid: string, dateId: string) {
  return deleteDoc(doc(db, userPrefix(uid), 'importantDates', dateId))
}

// Semester
export function semesterRef(uid: string) {
  return doc(db, userPrefix(uid), 'semester', 'current')
}
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "feat: add types and firestore utilities"
```

---

### Task 7: 课程表 Stores 移植 (semester + timeSlot + course)

**Files:**
- Create: `daily-dashboard/src/stores/semester.ts`
- Create: `daily-dashboard/src/stores/timeSlot.ts`
- Create: `daily-dashboard/src/stores/course.ts`

- [ ] **Step 1: Create semester store**

```ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import { getDoc, setDoc } from 'firebase/firestore'
import { db } from '@/firebase/init'
import { useAuthStore } from './auth'
import { semesterRef } from '@/utils/firestore'

export const useSemesterStore = defineStore('semester', () => {
  const currentWeek = ref(1)
  const semesterName = ref('')
  const startDate = ref('')
  const totalWeeks = ref(20)

  const weekDateRange = computed(() => {
    if (!startDate.value) return ''
    const start = dayjs(startDate.value)
    const weekStart = start.add((currentWeek.value - 1) * 7, 'day')
    const weekEnd = weekStart.add(6, 'day')
    return `${weekStart.format('M/D')} - ${weekEnd.format('M/D')}`
  })

  async function fetchSemester() {
    const auth = useAuthStore()
    if (!auth.user) return
    const snap = await getDoc(semesterRef(auth.user.uid))
    if (snap.exists()) {
      const d = snap.data()
      semesterName.value = d.name || ''
      startDate.value = d.startDate?.toDate?.()?.toISOString() || ''
      totalWeeks.value = d.totalWeeks || 20
      if (d.startDate?.toDate) {
        const diff = dayjs().diff(dayjs(d.startDate.toDate()), 'week')
        currentWeek.value = Math.max(1, Math.min(diff + 1, totalWeeks.value))
      }
    }
  }

  async function saveSemester(data: { name: string; startDate: string; totalWeeks: number }) {
    const auth = useAuthStore()
    if (!auth.user) return
    await setDoc(semesterRef(auth.user.uid), {
      name: data.name,
      startDate: new Date(data.startDate),
      totalWeeks: data.totalWeeks,
    })
    semesterName.value = data.name
    startDate.value = data.startDate
    totalWeeks.value = data.totalWeeks
  }

  function setCurrentWeek(n: number) {
    currentWeek.value = Math.max(1, Math.min(n, totalWeeks.value))
  }

  function nextWeek() {
    setCurrentWeek(currentWeek.value + 1)
  }

  function prevWeek() {
    setCurrentWeek(currentWeek.value - 1)
  }

  function goToCurrentWeek() {
    if (startDate.value) {
      const diff = dayjs().diff(dayjs(startDate.value), 'week')
      currentWeek.value = Math.max(1, Math.min(diff + 1, totalWeeks.value))
    }
  }

  return {
    currentWeek, semesterName, startDate, totalWeeks,
    weekDateRange, fetchSemester, saveSemester,
    setCurrentWeek, nextWeek, prevWeek, goToCurrentWeek,
  }
})
```

- [ ] **Step 2: Create timeSlot store**

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TimeSlot } from '@/types'
import { DEFAULT_TIME_SLOTS } from '@/types'
import { subscribeTimeSlots, timeSlotsRef } from '@/utils/firestore'
import { useAuthStore } from './auth'
import { getDocs, writeBatch, doc } from 'firebase/firestore'
import { db } from '@/firebase/init'

export const useTimeSlotStore = defineStore('timeSlot', () => {
  const timeSlots = ref<TimeSlot[]>([])
  const loading = ref(true)
  let unsub: (() => void) | null = null

  function fetchTimeSlots() {
    const auth = useAuthStore()
    if (!auth.user) return
    const uid = auth.user.uid
    loading.value = true
    unsub?.()
    unsub = subscribeTimeSlots(uid, (list) => {
      if (list.length === 0) {
        initDefaultSlots(uid)
      } else {
        timeSlots.value = list
        loading.value = false
      }
    })
  }

  async function initDefaultSlots(uid: string) {
    const batch = writeBatch(db)
    const ref = timeSlotsRef(uid)
    for (const slot of DEFAULT_TIME_SLOTS) {
      batch.set(doc(ref), slot)
    }
    await batch.commit()
    loading.value = false
  }

  function stopListening() {
    unsub?.()
    unsub = null
  }

  async function updateTimeSlots(slots: Omit<TimeSlot, 'id'>[]) {
    const auth = useAuthStore()
    if (!auth.user) return
    const existing = await getDocs(timeSlotsRef(auth.user.uid))
    const batch = writeBatch(db)
    existing.forEach((d) => batch.delete(d.ref))
    for (const s of slots) {
      batch.set(doc(timeSlotsRef(auth.user.uid)), s)
    }
    await batch.commit()
  }

  return { timeSlots, loading, fetchTimeSlots, stopListening, updateTimeSlots }
})
```

- [ ] **Step 3: Create course store**

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Course, Schedule } from '@/types'
import { subscribeCourses, addCourse as fbAdd, updateCourse as fbUpdate, deleteCourse as fbDelete } from '@/utils/firestore'
import { useAuthStore } from './auth'

export const useCourseStore = defineStore('course', () => {
  const courses = ref<Course[]>([])
  const loading = ref(true)
  let unsub: (() => void) | null = null

  function fetchCourses() {
    const auth = useAuthStore()
    if (!auth.user) return
    loading.value = true
    unsub?.()
    unsub = subscribeCourses(auth.user.uid, (list) => {
      courses.value = list
      loading.value = false
    })
  }

  function stopListening() {
    unsub?.()
    unsub = null
  }

  async function addCourse(course: Omit<Course, 'id'>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbAdd(auth.user.uid, course)
  }

  async function updateCourse(courseId: string, data: Partial<Course>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbUpdate(auth.user.uid, courseId, data)
  }

  async function deleteCourse(courseId: string) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbDelete(auth.user.uid, courseId)
  }

  function isScheduleVisible(schedule: Schedule, weekNum: number) {
    if (schedule.weekType === 'all') return true
    if (schedule.weekType === 'odd') return weekNum % 2 === 1
    if (schedule.weekType === 'even') return weekNum % 2 === 0
    if (schedule.weekType === 'custom') return schedule.customWeeks?.includes(weekNum) ?? false
    return false
  }

  return { courses, loading, fetchCourses, stopListening, addCourse, updateCourse, deleteCourse, isScheduleVisible }
})
```

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "feat: port course-related stores (semester, timeSlot, course)"
```

---

### Task 8: 课程表组件移植

**Files:**
- Create: `daily-dashboard/src/components/course/CourseCard.vue`
- Create: `daily-dashboard/src/components/course/GridHeader.vue`
- Create: `daily-dashboard/src/components/course/TimeColumn.vue`
- Create: `daily-dashboard/src/components/course/CourseGrid.vue`
- Create: `daily-dashboard/src/components/course/CourseDialog.vue`
- Create: `daily-dashboard/src/components/course/TimeSlotEditor.vue`
- Create: `daily-dashboard/src/views/CourseGridView.vue`

Copy these files directly from the class-table project's `src/components/grid/` and `src/components/` directories, adjusting any import paths from `@/components/grid/` to `@/components/course/`.

- [ ] **Step 1: Copy CourseCard.vue**

Copy `d:\code\claude\class-table\src\components\grid\CourseCard.vue` → `daily-dashboard/src/components/course/CourseCard.vue`
No changes needed besides the path.

- [ ] **Step 2: Copy GridHeader.vue**

Copy `d:\code\claude\class-table\src\components\grid\GridHeader.vue` → `daily-dashboard/src/components/course/GridHeader.vue`

- [ ] **Step 3: Copy TimeColumn.vue**

Copy `d:\code\claude\class-table\src\components\grid\TimeColumn.vue` → `daily-dashboard/src/components/course/TimeColumn.vue`

- [ ] **Step 4: Copy CourseGrid.vue**

Copy `d:\code\claude\class-table\src\components\grid\CourseGrid.vue` → `daily-dashboard/src/components/course/CourseGrid.vue`
Update import paths from `@/components/grid/` to `@/components/course/`.

- [ ] **Step 5: Copy CourseDialog.vue and TimeSlotEditor.vue**

Copy `d:\code\claude\class-table\src\components\CourseDialog.vue` → `daily-dashboard/src/components/course/CourseDialog.vue`
Copy `d:\code\claude\class-table\src\components\TimeSlotEditor.vue` → `daily-dashboard/src/components/course/TimeSlotEditor.vue`

- [ ] **Step 6: Create CourseGridView.vue**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import type { Course } from '@/types'
import CourseGrid from '@/components/course/CourseGrid.vue'
import CourseDialog from '@/components/course/CourseDialog.vue'
import { useCourseStore } from '@/stores/course'
import { useSemesterStore } from '@/stores/semester'
import { useTimeSlotStore } from '@/stores/timeSlot'
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const courseStore = useCourseStore()
const semesterStore = useSemesterStore()
const timeSlotStore = useTimeSlotStore()
const dialogVisible = ref(false)
const editingCourse = ref<Course | null>(null)
const defaultDay = ref(1)
const defaultSlot = ref(1)

onMounted(() => {
  courseStore.fetchCourses()
  semesterStore.fetchSemester()
  timeSlotStore.fetchTimeSlots()
})

function handleAddCourse(dayOfWeek: number, slotIndex: number) {
  defaultDay.value = dayOfWeek
  defaultSlot.value = slotIndex + 1
  editingCourse.value = null
  dialogVisible.value = true
}

function handleEditCourse(courseId: string) {
  const course = courseStore.courses.find((c) => c.id === courseId)
  if (course) {
    editingCourse.value = JSON.parse(JSON.stringify(course))
    dialogVisible.value = true
  }
}

async function handleSave(data: Omit<Course, 'id'>) {
  try {
    if (editingCourse.value?.id) {
      await courseStore.updateCourse(editingCourse.value.id, data)
      ElMessage.success('课程已更新')
    } else {
      await courseStore.addCourse(data)
      ElMessage.success('课程已添加')
    }
    dialogVisible.value = false
  } catch {
    ElMessage.error('保存失败')
  }
}

async function handleDelete() {
  if (editingCourse.value?.id) {
    try {
      await courseStore.deleteCourse(editingCourse.value.id)
      ElMessage.success('课程已删除')
      dialogVisible.value = false
    } catch {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<template>
  <div class="course-grid-view">
    <div class="view-header">
      <h2>课表</h2>
    </div>
    <CourseGrid
      @add-course="handleAddCourse"
      @edit-course="handleEditCourse"
    />
    <CourseDialog
      v-model:visible="dialogVisible"
      :course="editingCourse"
      :defaultDay="defaultDay"
      :defaultSlot="defaultSlot"
      @save="handleSave"
      @delete="handleDelete"
    />
  </div>
</template>

<style scoped>
.course-grid-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.view-header {
  margin-bottom: 16px;
}
.view-header h2 {
  font-size: 20px;
  color: var(--el-text-color-primary);
}
</style>
```

- [ ] **Step 7: Commit**

```bash
git add .
git commit -m "feat: port course grid components and view"
```

---

### Task 9: 日程管理 (Calendar)

**Files:**
- Create: `daily-dashboard/src/stores/calendar.ts`
- Create: `daily-dashboard/src/views/CalendarView.vue`

- [ ] **Step 1: Create calendar store**

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CalendarEvent } from '@/types'
import {
  subscribeCalendar,
  addCalendarEvent as fbAdd,
  updateCalendarEvent as fbUpdate,
  deleteCalendarEvent as fbDelete,
} from '@/utils/firestore'
import { useAuthStore } from './auth'

export const useCalendarStore = defineStore('calendar', () => {
  const events = ref<CalendarEvent[]>([])
  const loading = ref(true)
  let unsub: (() => void) | null = null

  function fetchEvents() {
    const auth = useAuthStore()
    if (!auth.user) return
    loading.value = true
    unsub?.()
    unsub = subscribeCalendar(auth.user.uid, (list) => {
      events.value = list
      loading.value = false
    })
  }

  function stopListening() {
    unsub?.()
    unsub = null
  }

  async function addEvent(event: Omit<CalendarEvent, 'id'>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbAdd(auth.user.uid, event)
  }

  async function updateEvent(eventId: string, data: Partial<CalendarEvent>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbUpdate(auth.user.uid, eventId, data)
  }

  async function deleteEvent(eventId: string) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbDelete(auth.user.uid, eventId)
  }

  return { events, loading, fetchEvents, stopListening, addEvent, updateEvent, deleteEvent }
})
```

- [ ] **Step 2: Create CalendarView.vue**

A simple day-based event list view. Shows events grouped by date, with ability to add/edit/delete.

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useCalendarStore } from '@/stores/calendar'
import { ElMessage } from 'element-plus'

const store = useCalendarStore()
const dialogVisible = ref(false)
const editingEvent = ref<any>(null)
const form = ref({ title: '', date: '', time: '', note: '' })

const today = dayjs().format('YYYY-MM-DD')

onMounted(() => {
  store.fetchEvents()
})

const groupedEvents = computed(() => {
  const groups: Record<string, typeof store.events> = {}
  for (const ev of store.events) {
    const dateStr = ev.date?.toDate?.() ? dayjs(ev.date.toDate()).format('YYYY-MM-DD') : ''
    if (!dateStr) continue
    if (!groups[dateStr]) groups[dateStr] = []
    groups[dateStr].push(ev)
  }
  return Object.entries(groups).sort(([a], [b]) => a.localeCompare(b))
})

function openAdd() {
  editingEvent.value = null
  form.value = { title: '', date: today, time: '', note: '' }
  dialogVisible.value = true
}

function openEdit(ev: any) {
  editingEvent.value = ev
  const d = ev.date?.toDate ? dayjs(ev.date.toDate()) : dayjs()
  form.value = {
    title: ev.title || '',
    date: d.format('YYYY-MM-DD'),
    time: ev.time || '',
    note: ev.note || '',
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const data = {
      title: form.value.title,
      date: new Date(form.value.date),
      time: form.value.time || '',
      note: form.value.note || '',
    }
    if (editingEvent.value?.id) {
      await store.updateEvent(editingEvent.value.id, data)
      ElMessage.success('已更新')
    } else {
      await store.addEvent(data)
      ElMessage.success('已添加')
    }
    dialogVisible.value = false
  } catch {
    ElMessage.error('保存失败')
  }
}

async function handleDelete() {
  if (editingEvent.value?.id) {
    try {
      await store.deleteEvent(editingEvent.value.id)
      ElMessage.success('已删除')
      dialogVisible.value = false
    } catch {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<template>
  <div class="calendar-view">
    <div class="view-header">
      <h2>日程</h2>
      <el-button type="primary" @click="openAdd">添加日程</el-button>
    </div>

    <div v-if="groupedEvents.length === 0" class="empty">
      暂无日程
    </div>

    <div v-for="[date, items] in groupedEvents" :key="date" class="day-group">
      <h3 class="day-header">{{ dayjs(date).format('M月D日 dddd') }}</h3>
      <div
        v-for="ev in items"
        :key="ev.id"
        class="event-card"
        @click="openEdit(ev)"
      >
        <span v-if="ev.time" class="event-time">{{ ev.time }}</span>
        <span class="event-title">{{ ev.title }}</span>
        <span v-if="ev.note" class="event-note">{{ ev.note }}</span>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingEvent ? '编辑日程' : '添加日程'"
      width="400px"
    >
      <el-form :model="form">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="时间">
          <el-time-picker v-model="form.time" value-format="HH:mm" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="editingEvent" type="danger" @click="handleDelete">删除</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.calendar-view { max-width: 800px; }
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.view-header h2 { font-size: 20px; color: var(--el-text-color-primary); }
.empty { color: var(--el-text-color-secondary); padding: 40px; text-align: center; }
.day-group { margin-bottom: 24px; }
.day-header {
  font-size: 15px;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--el-border-color-light);
}
.event-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
}
.event-card:hover { background: var(--el-fill-color-light); }
.event-time {
  font-size: 13px;
  color: var(--el-color-primary);
  font-weight: 500;
  min-width: 50px;
}
.event-title { font-size: 14px; color: var(--el-text-color-primary); }
.event-note {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: auto;
}
</style>
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "feat: add calendar event management"
```

---

### Task 10: 待办管理 (Todo)

**Files:**
- Create: `daily-dashboard/src/stores/todo.ts`
- Create: `daily-dashboard/src/views/TodoView.vue`

- [ ] **Step 1: Create todo store**

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TodoItem } from '@/types'
import {
  subscribeTodos,
  addTodo as fbAdd,
  updateTodo as fbUpdate,
  deleteTodo as fbDelete,
} from '@/utils/firestore'
import { useAuthStore } from './auth'

export const useTodoStore = defineStore('todo', () => {
  const todos = ref<TodoItem[]>([])
  const loading = ref(true)
  let unsub: (() => void) | null = null

  function fetchTodos() {
    const auth = useAuthStore()
    if (!auth.user) return
    loading.value = true
    unsub?.()
    unsub = subscribeTodos(auth.user.uid, (list) => {
      todos.value = list
      loading.value = false
    })
  }

  function stopListening() {
    unsub?.()
    unsub = null
  }

  async function addTodo(todo: Omit<TodoItem, 'id'>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbAdd(auth.user.uid, todo)
  }

  async function updateTodo(todoId: string, data: Partial<TodoItem>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbUpdate(auth.user.uid, todoId, data)
  }

  async function deleteTodo(todoId: string) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbDelete(auth.user.uid, todoId)
  }

  async function toggleDone(todoId: string, current: boolean) {
    await updateTodo(todoId, { done: !current })
  }

  return { todos, loading, fetchTodos, stopListening, addTodo, updateTodo, deleteTodo, toggleDone }
})
```

- [ ] **Step 2: Create TodoView.vue**

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useTodoStore } from '@/stores/todo'
import { ElMessage } from 'element-plus'

const store = useTodoStore()
const filter = ref<'all' | 'active' | 'done'>('all')
const newContent = ref('')
const editingId = ref<string | null>(null)
const editContent = ref('')

onMounted(() => store.fetchTodos())

const filteredTodos = computed(() => {
  let list = store.todos
  if (filter.value === 'active') list = list.filter(t => !t.done)
  else if (filter.value === 'done') list = list.filter(t => t.done)
  return list
})

async function addTodo() {
  const content = newContent.value.trim()
  if (!content) return
  await store.addTodo({ content, done: false, createdAt: new Date() })
  newContent.value = ''
}

async function handleToggle(todo: any) {
  await store.toggleDone(todo.id, todo.done)
}

function startEdit(todo: any) {
  editingId.value = todo.id
  editContent.value = todo.content
}

async function saveEdit(id: string) {
  const content = editContent.value.trim()
  if (!content) return
  await store.updateTodo(id, { content })
  editingId.value = null
}

async function handleDelete(id: string) {
  await store.deleteTodo(id)
}
</script>

<template>
  <div class="todo-view">
    <div class="view-header">
      <h2>待办</h2>
    </div>

    <div class="add-bar">
      <el-input v-model="newContent" placeholder="添加待办..." @keyup.enter="addTodo" />
      <el-button type="primary" @click="addTodo">添加</el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filter">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="active">未完成</el-radio-button>
        <el-radio-button value="done">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="filteredTodos.length === 0" class="empty">
      暂无待办
    </div>

    <div v-for="todo in filteredTodos" :key="todo.id" class="todo-item" :class="{ done: todo.done }">
      <el-checkbox :model-value="todo.done" @change="handleToggle(todo)" />
      <template v-if="editingId === todo.id">
        <el-input v-model="editContent" size="small" @keyup.enter="saveEdit(todo.id)" @blur="saveEdit(todo.id)" />
      </template>
      <template v-else>
        <span class="todo-content" @click="startEdit(todo)">{{ todo.content }}</span>
      </template>
      <span v-if="todo.dueDate?.toDate" class="todo-due">
        截止: {{ dayjs(todo.dueDate.toDate()).format('M/D') }}
      </span>
      <el-button text type="danger" size="small" @click="handleDelete(todo.id)">删除</el-button>
    </div>
  </div>
</template>

<style scoped>
.todo-view { max-width: 600px; }
.view-header { margin-bottom: 16px; }
.view-header h2 { font-size: 20px; color: var(--el-text-color-primary); }
.add-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.filter-bar { margin-bottom: 16px; }
.empty { color: var(--el-text-color-secondary); padding: 40px; text-align: center; }
.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  transition: background 0.15s;
  margin-bottom: 4px;
}
.todo-item:hover { background: var(--el-fill-color-light); }
.todo-item.done .todo-content {
  text-decoration: line-through;
  color: var(--el-text-color-disabled);
}
.todo-content { flex: 1; cursor: pointer; }
.todo-due { font-size: 12px; color: var(--el-text-color-secondary); }
</style>
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "feat: add todo management"
```

---

### Task 11: 重大日期管理 (ImportantDates)

**Files:**
- Create: `daily-dashboard/src/stores/importantDates.ts`
- Create: `daily-dashboard/src/views/ImportantDatesView.vue`

- [ ] **Step 1: Create importantDates store**

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ImportantDate } from '@/types'
import {
  subscribeImportantDates,
  addImportantDate as fbAdd,
  updateImportantDate as fbUpdate,
  deleteImportantDate as fbDelete,
} from '@/utils/firestore'
import { useAuthStore } from './auth'

export const useImportantDateStore = defineStore('importantDates', () => {
  const dates = ref<ImportantDate[]>([])
  const loading = ref(true)
  let unsub: (() => void) | null = null

  function fetchDates() {
    const auth = useAuthStore()
    if (!auth.user) return
    loading.value = true
    unsub?.()
    unsub = subscribeImportantDates(auth.user.uid, (list) => {
      dates.value = list
      loading.value = false
    })
  }

  function stopListening() {
    unsub?.()
    unsub = null
  }

  async function addDate(date: Omit<ImportantDate, 'id'>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbAdd(auth.user.uid, date)
  }

  async function updateDate(dateId: string, data: Partial<ImportantDate>) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbUpdate(auth.user.uid, dateId, data)
  }

  async function deleteDate(dateId: string) {
    const auth = useAuthStore()
    if (!auth.user) return
    await fbDelete(auth.user.uid, dateId)
  }

  return { dates, loading, fetchDates, stopListening, addDate, updateDate, deleteDate }
})
```

- [ ] **Step 2: Create ImportantDatesView.vue**

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useImportantDateStore } from '@/stores/importantDates'
import { ElMessage } from 'element-plus'

const store = useImportantDateStore()
const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const form = ref({ name: '', date: '', color: '#8FA88F', type: 'custom' as const, note: '' })

onMounted(() => store.fetchDates())

const upcoming = computed(() =>
  store.dates
    .filter(d => {
      const dt = d.date?.toDate ? d.date.toDate() : null
      return dt && dt >= new Date()
    })
    .sort((a, b) => {
      const da = a.date?.toDate ? a.date.toDate().getTime() : 0
      const db = b.date?.toDate ? b.date.toDate().getTime() : 0
      return da - db
    })
)

const past = computed(() =>
  store.dates
    .filter(d => {
      const dt = d.date?.toDate ? d.date.toDate() : null
      return dt && dt < new Date()
    })
    .sort((a, b) => {
      const da = a.date?.toDate ? a.date.toDate().getTime() : 0
      const db = b.date?.toDate ? b.date.toDate().getTime() : 0
      return db - da
    })
)

function openAdd() {
  editingId.value = null
  form.value = { name: '', date: '', color: '#8FA88F', type: 'custom', note: '' }
  dialogVisible.value = true
}

function openEdit(d: any) {
  editingId.value = d.id
  const dt = d.date?.toDate ? dayjs(d.date.toDate()) : dayjs()
  form.value = {
    name: d.name || '',
    date: dt.format('YYYY-MM-DD'),
    color: d.color || '#8FA88F',
    type: d.type || 'custom',
    note: d.note || '',
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const data = {
      name: form.value.name,
      date: new Date(form.value.date),
      color: form.value.color,
      type: form.value.type,
      note: form.value.note,
    }
    if (editingId.value) {
      await store.updateDate(editingId.value, data)
      ElMessage.success('已更新')
    } else {
      await store.addDate(data)
      ElMessage.success('已添加')
    }
    dialogVisible.value = false
  } catch {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(id: string) {
  await store.deleteDate(id)
  dialogVisible.value = false
}
</script>

<template>
  <div class="dates-view">
    <div class="view-header">
      <h2>重大日期</h2>
      <el-button type="primary" @click="openAdd">添加日期</el-button>
    </div>

    <h3 class="section-title">即将到来</h3>
    <div v-if="upcoming.length === 0" class="empty">暂无即将到来的日期</div>
    <div v-for="d in upcoming" :key="d.id" class="date-card" @click="openEdit(d)">
      <div class="date-color" :style="{ background: d.color }" />
      <div class="date-info">
        <span class="date-name">{{ d.name }}</span>
        <span class="date-value">{{ d.date?.toDate ? dayjs(d.date.toDate()).format('YYYY年M月D日') : '' }}</span>
      </div>
      <span class="date-countdown">
        还剩 {{ d.date?.toDate ? dayjs(d.date.toDate()).diff(dayjs(), 'day') + 1 : '?' }} 天
      </span>
    </div>

    <h3 class="section-title" v-if="past.length">已过去</h3>
    <div v-for="d in past" :key="d.id" class="date-card past" @click="openEdit(d)">
      <div class="date-color" :style="{ background: d.color }" />
      <div class="date-info">
        <span class="date-name">{{ d.name }}</span>
        <span class="date-value">{{ d.date?.toDate ? dayjs(d.date.toDate()).format('YYYY年M月D日') : '' }}</span>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑日期' : '添加日期'"
      width="400px"
    >
      <el-form :model="form">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option value="birthday" label="生日" />
            <el-option value="anniversary" label="纪念日" />
            <el-option value="custom" label="自定义" />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="editingId" type="danger" @click="handleDelete(editingId)">删除</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dates-view { max-width: 600px; }
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.view-header h2 { font-size: 20px; color: var(--el-text-color-primary); }
.section-title {
  font-size: 15px;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
  margin-top: 16px;
}
.empty { color: var(--el-text-color-secondary); padding: 20px; text-align: center; }
.date-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 8px;
}
.date-card:hover { background: var(--el-fill-color-light); }
.date-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}
.date-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.date-name { font-size: 14px; color: var(--el-text-color-primary); font-weight: 500; }
.date-value { font-size: 12px; color: var(--el-text-color-secondary); }
.date-countdown { font-size: 13px; color: var(--el-color-primary); font-weight: 500; white-space: nowrap; }
.past { opacity: 0.5; }
</style>
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "feat: add important dates management"
```

---

### Phase 3 — Dashboard & API

### Task 12: 天气模块

**Files:**
- Create: `daily-dashboard/src/utils/weather.ts`
- Create: `daily-dashboard/src/stores/weather.ts`
- Create: `daily-dashboard/src/components/widgets/WeatherWidget.vue`

- [ ] **Step 1: Create weather API utility**

```ts
const BASE = 'https://api.openweathermap.org/data/2.5'

function getKey(): string {
  return import.meta.env.VITE_OPENWEATHERMAP_API_KEY || ''
}

export interface WeatherData {
  temp: number
  feelsLike: number
  humidity: number
  description: string
  icon: string
  city: string
}

export interface ForecastDay {
  date: string
  tempMax: number
  tempMin: number
  icon: string
  description: string
}

export async function fetchCurrentWeather(city: string): Promise<WeatherData | null> {
  const key = getKey()
  if (!key) return null
  const res = await fetch(`${BASE}/weather?q=${city}&units=metric&lang=zh_cn&appid=${key}`)
  if (!res.ok) return null
  const d = await res.json()
  return {
    temp: Math.round(d.main.temp),
    feelsLike: Math.round(d.main.feels_like),
    humidity: d.main.humidity,
    description: d.weather[0].description,
    icon: d.weather[0].icon,
    city: d.name,
  }
}

export async function fetchForecast(city: string): Promise<ForecastDay[]> {
  const key = getKey()
  if (!key) return []
  const res = await fetch(`${BASE}/forecast?q=${city}&units=metric&lang=zh_cn&appid=${key}`)
  if (!res.ok) return []
  const d = await res.json()
  const daily: Record<string, ForecastDay> = {}
  for (const item of d.list) {
    const date = item.dt_txt.split(' ')[0]
    if (!daily[date]) {
      daily[date] = {
        date,
        tempMax: item.main.temp_max,
        tempMin: item.main.temp_min,
        icon: item.weather[0].icon,
        description: item.weather[0].description,
      }
    } else {
      daily[date].tempMax = Math.max(daily[date].tempMax, item.main.temp_max)
      daily[date].tempMin = Math.min(daily[date].tempMin, item.main.temp_min)
    }
  }
  return Object.values(daily).slice(0, 5)
}
```

- [ ] **Step 2: Create weather store**

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { WeatherData, ForecastDay } from '@/utils/weather'
import { fetchCurrentWeather, fetchForecast } from '@/utils/weather'

const CACHE_KEY = 'dashboard_weather'
const CACHE_TTL = 30 * 60 * 1000 // 30 minutes

export const useWeatherStore = defineStore('weather', () => {
  const current = ref<WeatherData | null>(null)
  const forecast = ref<ForecastDay[]>([])
  const loading = ref(false)
  const error = ref('')
  const city = ref(localStorage.getItem('weather_city') || '北京')

  async function refresh(force = false) {
    const cached = localStorage.getItem(CACHE_KEY)
    if (!force && cached) {
      try {
        const parsed = JSON.parse(cached)
        if (Date.now() - parsed.timestamp < CACHE_TTL) {
          current.value = parsed.current
          forecast.value = parsed.forecast
          return
        }
      } catch { /* ignore */ }
    }

    loading.value = true
    error.value = ''
    try {
      const [curr, fore] = await Promise.all([
        fetchCurrentWeather(city.value),
        fetchForecast(city.value),
      ])
      if (curr) current.value = curr
      if (fore.length) forecast.value = fore
      if (curr && fore.length) {
        localStorage.setItem(CACHE_KEY, JSON.stringify({
          current: curr, forecast: fore, timestamp: Date.now(),
        }))
      }
    } catch {
      error.value = '获取天气失败'
    } finally {
      loading.value = false
    }
  }

  function setCity(c: string) {
    city.value = c
    localStorage.setItem('weather_city', c)
    refresh(true)
  }

  return { current, forecast, loading, error, city, refresh, setCity }
})
```

- [ ] **Step 3: Create WeatherWidget.vue**

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { useWeatherStore } from '@/stores/weather'

const weather = useWeatherStore()

onMounted(() => weather.refresh())
</script>

<template>
  <div class="weather-widget">
    <template v-if="weather.loading && !weather.current">
      <span class="loading">加载天气...</span>
    </template>
    <template v-else-if="weather.current">
      <div class="weather-main">
        <img
          v-if="weather.current.icon"
          :src="`https://openweathermap.org/img/wn/${weather.current.icon}@2x.png`"
          class="weather-icon"
          alt=""
        />
        <div class="weather-temp">{{ weather.current.temp }}°C</div>
        <div class="weather-desc">{{ weather.current.description }}</div>
      </div>
      <div class="weather-details">
        <span>体感 {{ weather.current.feelsLike }}°C</span>
        <span>湿度 {{ weather.current.humidity }}%</span>
        <span class="weather-city">{{ weather.current.city }}</span>
      </div>
    </template>
    <div v-else-if="weather.error" class="error">{{ weather.error }}</div>
  </div>
</template>

<style scoped>
.weather-widget {
  display: flex;
  align-items: center;
  gap: 16px;
}
.weather-main {
  display: flex;
  align-items: center;
  gap: 8px;
}
.weather-icon { width: 40px; height: 40px; }
.weather-temp { font-size: 24px; font-weight: 700; }
.weather-desc { font-size: 14px; color: var(--el-text-color-secondary); }
.weather-details {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.weather-city { color: var(--el-text-color-primary); }
.loading, .error { font-size: 14px; color: var(--el-text-color-secondary); }
.error { color: var(--el-color-danger); }
</style>
```

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "feat: add weather module with OpenWeatherMap API"
```

---

### Task 13: CSGO 赛事模块

**Files:**
- Create: `daily-dashboard/src/utils/pandascore.ts`
- Create: `daily-dashboard/src/stores/csgo.ts`
- Create: `daily-dashboard/src/components/widgets/CsgoWidget.vue`
- Create: `daily-dashboard/public/csgo-events.json`

- [ ] **Step 1: Create PandaScore API utility**

```ts
import type { CsgoEvent } from '@/types'

const BASE = 'https://api.pandascore.co'
const PER_PAGE = 50

function getKey(): string {
  return import.meta.env.VITE_PANDASCORE_API_KEY || ''
}

export interface PandaScoreTournament {
  id: number
  name: string
  begin_at: string
  end_at: string
  league?: {
    name: string
    image_url?: string
  }
  videogame?: {
    name: string
  }
  status: 'running' | 'upcoming' | 'finished'
}

export async function fetchUpcomingTournaments(): Promise<CsgoEvent[]> {
  const key = getKey()
  if (!key) return []
  const res = await fetch(
    `${BASE}/csgo/tournaments?filter[status]=upcoming,running&sort=begin_at&per_page=${PER_PAGE}`,
    { headers: { Authorization: `Bearer ${key}` } }
  )
  if (!res.ok) return []
  const data: PandaScoreTournament[] = await res.json()
  return data.map(t => ({
    id: String(t.id),
    name: t.name,
    startDate: t.begin_at?.split('T')[0] || '',
    endDate: t.end_at?.split('T')[0] || '',
    logo: t.league?.image_url || '',
  }))
}
```

- [ ] **Step 2: Create csgo-events.json fallback**

```json
[
  {
    "id": "iem-cologne-2026",
    "name": "IEM Cologne 2026",
    "startDate": "2026-07-20",
    "endDate": "2026-08-04",
    "logo": ""
  },
  {
    "id": "esl-pro-league-s21",
    "name": "ESL Pro League Season 21",
    "startDate": "2026-09-01",
    "endDate": "2026-09-22",
    "logo": ""
  },
  {
    "id": "blast-premier-fall-2026",
    "name": "BLAST Premier Fall Final 2026",
    "startDate": "2026-10-15",
    "endDate": "2026-10-20",
    "logo": ""
  },
  {
    "id": "pgl-major-2026",
    "name": "PGL Major 2026",
    "startDate": "2026-11-15",
    "endDate": "2026-11-30",
    "logo": ""
  },
  {
    "id": "blast-world-final-2026",
    "name": "BLAST Premier World Final 2026",
    "startDate": "2026-12-10",
    "endDate": "2026-12-15",
    "logo": ""
  }
]
```

- [ ] **Step 3: Create CSGO store**

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CsgoEvent } from '@/types'
import { fetchUpcomingTournaments } from '@/utils/pandascore'
import fallbackEvents from '/csgo-events.json'

const CACHE_KEY = 'dashboard_csgo'
const CACHE_TTL = 6 * 60 * 60 * 1000 // 6 hours

export const useCsgoStore = defineStore('csgo', () => {
  const events = ref<CsgoEvent[]>(fallbackEvents as CsgoEvent[])
  const loading = ref(false)
  const error = ref('')

  async function refresh(force = false) {
    const cached = localStorage.getItem(CACHE_KEY)
    if (!force && cached) {
      try {
        const parsed = JSON.parse(cached)
        if (Date.now() - parsed.timestamp < CACHE_TTL) {
          events.value = parsed.events
          return
        }
      } catch { /* ignore */ }
    }

    loading.value = true
    error.value = ''
    try {
      const apiEvents = await fetchUpcomingTournaments()
      if (apiEvents.length > 0) {
        events.value = apiEvents
        localStorage.setItem(CACHE_KEY, JSON.stringify({
          events: apiEvents, timestamp: Date.now(),
        }))
      }
    } catch {
      error.value = '获取赛事数据失败，使用本地数据'
    } finally {
      loading.value = false
    }
  }

  const now = new Date()

  const upcomingEvents = ref<CsgoEvent[]>([])
  const liveEvents = ref<CsgoEvent[]>([])

  function processEvents() {
    const today = new Date().toISOString().split('T')[0]
    upcomingEvents.value = events.value.filter(e => e.endDate >= today)
      .sort((a, b) => a.startDate.localeCompare(b.startDate))
    liveEvents.value = events.value.filter(e => e.startDate <= today && e.endDate >= today)
  }

  return { events, loading, error, upcomingEvents, liveEvents, refresh, processEvents }
})
```

- [ ] **Step 4: Create CsgoWidget.vue**

```vue
<script setup lang="ts">
import { computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useCsgoStore } from '@/stores/csgo'

const csgo = useCsgoStore()

onMounted(() => {
  csgo.refresh()
  csgo.processEvents()
})

const displayEvents = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  const upcoming = csgo.events.filter(e => e.endDate >= today)
    .sort((a, b) => a.startDate.localeCompare(b.startDate))
    .slice(0, 5)
  return upcoming
})

function isLive(event: { startDate: string; endDate: string }) {
  const today = new Date().toISOString().split('T')[0]
  return event.startDate <= today && event.endDate >= today
}

function countdown(dateStr: string) {
  const days = dayjs(dateStr).diff(dayjs(), 'day')
  if (days < 0) return '已结束'
  if (days === 0) return '今天开始'
  return `还剩 ${days} 天`
}
</script>

<template>
  <div class="csgo-widget">
    <div v-if="csgo.loading && csgo.events.length === 0" class="loading">
      加载赛事数据...
    </div>
    <div v-else-if="displayEvents.length === 0" class="empty">
      暂无即将到来的赛事
    </div>
    <div
      v-for="ev in displayEvents"
      :key="ev.id"
      class="event-item"
      :class="{ live: isLive(ev) }"
    >
      <div class="event-header">
        <span v-if="isLive(ev)" class="live-badge">LIVE</span>
        <span class="event-name">{{ ev.name }}</span>
      </div>
      <div class="event-meta">
        <span class="event-dates">
          {{ dayjs(ev.startDate).format('M/D') }} - {{ dayjs(ev.endDate).format('M/D') }}
        </span>
        <span class="event-countdown">{{ countdown(ev.startDate) }}</span>
      </div>
    </div>
    <div v-if="csgo.error" class="error-note">{{ csgo.error }}</div>
  </div>
</template>

<style scoped>
.csgo-widget { font-size: 14px; }
.loading, .empty {
  color: var(--el-text-color-secondary);
  padding: 20px;
  text-align: center;
  font-size: 13px;
}
.event-item {
  padding: 8px 0;
  border-bottom: 1px solid var(--el-border-color-light);
}
.event-item:last-child { border-bottom: none; }
.event-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}
.event-name { font-weight: 500; }
.live-badge {
  background: #e74c3c;
  color: white;
  font-size: 11px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 3px;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.event-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.event-countdown { color: var(--el-color-primary); font-weight: 500; }
.error-note { font-size: 12px; color: var(--el-color-warning); margin-top: 8px; }
</style>
```

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "feat: add CSGO events with PandaScore API and fallback"
```

---

### Task 14: Dashboard 主页 Widgets

**Files:**
- Create: `daily-dashboard/src/components/widgets/ScheduleWidget.vue`
- Create: `daily-dashboard/src/components/widgets/TodoWidget.vue`
- Create: `daily-dashboard/src/components/widgets/ImportantDatesWidget.vue`

- [ ] **Step 1: Create ScheduleWidget.vue**

Two tabs: "今日" shows today's courses + calendar events, "本周" shows weekly course thumbnail.

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useCourseStore } from '@/stores/course'
import { useSemesterStore } from '@/stores/semester'
import { useCalendarStore } from '@/stores/calendar'
import { useTimeSlotStore } from '@/stores/timeSlot'

const courseStore = useCourseStore()
const semesterStore = useSemesterStore()
const calendarStore = useCalendarStore()
const timeSlotStore = useTimeSlotStore()
const activeTab = ref<'today' | 'week'>('today')

onMounted(() => {
  courseStore.fetchCourses()
  semesterStore.fetchSemester()
  calendarStore.fetchEvents()
  timeSlotStore.fetchTimeSlots()
})

// Today's courses
const todayIndex = computed(() => {
  const d = dayjs().day()
  return d === 0 ? 6 : d - 1 // dayjs Sunday=0 → 0=Mon..6=Sun → our system: 0=Mon..6=Sun
})

const todayCourses = computed(() => {
  const dow = todayIndex.value + 1
  const week = semesterStore.currentWeek
  return courseStore.courses
    .filter(c => c.schedules?.some(s => s.dayOfWeek === dow && courseStore.isScheduleVisible(s, week)))
    .flatMap(c =>
      c.schedules
        .filter(s => s.dayOfWeek === dow && courseStore.isScheduleVisible(s, week))
        .map(s => ({
          name: c.name,
          location: c.location,
          color: c.color,
          startSlot: s.startSlot,
          duration: s.duration,
          startTime: timeSlotStore.timeSlots.find(t => t.slot === s.startSlot)?.startTime || '',
        }))
    )
    .sort((a, b) => a.startSlot - b.startSlot)
})

// Today's calendar events
const todayEvents = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return calendarStore.events.filter(ev => {
    const d = ev.date?.toDate ? dayjs(ev.date.toDate()).format('YYYY-MM-DD') : ''
    return d === today
  })
})

// Day labels for week
const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// Courses by day for the current week (compact display)
const weekCourses = computed(() => {
  const week = semesterStore.currentWeek
  const byDay: Record<number, { name: string; color: string }[]> = {}
  for (let d = 1; d <= 7; d++) {
    byDay[d] = courseStore.courses
      .filter(c => c.schedules?.some(s => s.dayOfWeek === d && courseStore.isScheduleVisible(s, week)))
      .map(c => ({ name: c.name, color: c.color }))
  }
  return byDay
})
</script>

<template>
  <div class="schedule-widget">
    <el-tabs v-model="activeTab" class="widget-tabs">
      <el-tab-pane label="今日" name="today">
        <div v-if="todayCourses.length === 0 && todayEvents.length === 0" class="empty">
          今天没有安排
        </div>
        <div
          v-for="(c, i) in todayCourses"
          :key="'c' + i"
          class="schedule-item"
        >
          <div class="schedule-time">{{ c.startTime }}</div>
          <div class="schedule-dot" :style="{ background: c.color }" />
          <div class="schedule-info">
            <span class="schedule-name">{{ c.name }}</span>
            <span v-if="c.location" class="schedule-location">{{ c.location }}</span>
          </div>
        </div>
        <div
          v-for="ev in todayEvents"
          :key="'e' + ev.id"
          class="schedule-item event"
        >
          <div class="schedule-time">{{ ev.time || '全天' }}</div>
          <div class="schedule-dot" style="background: var(--el-color-warning)" />
          <div class="schedule-info">
            <span class="schedule-name">{{ ev.title }}</span>
            <span v-if="ev.note" class="schedule-location">{{ ev.note }}</span>
          </div>
        </div>
        <router-link to="/course" class="view-more">查看完整课表 →</router-link>
      </el-tab-pane>
      <el-tab-pane label="本周" name="week">
        <div class="week-grid">
          <div v-for="d in 7" :key="d" class="week-col">
            <div class="week-col-header">{{ dayLabels[d - 1] }}</div>
            <div
              v-for="c in weekCourses[d]"
              :key="c.name + d"
              class="week-course-block"
              :style="{ background: c.color + '30', borderLeftColor: c.color }"
            >
              {{ c.name }}
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.schedule-widget { font-size: 14px; }
.widget-tabs { margin-top: -8px; }
.empty { color: var(--el-text-color-secondary); padding: 16px 0; text-align: center; font-size: 13px; }
.schedule-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}
.schedule-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  min-width: 48px;
  font-variant-numeric: tabular-nums;
}
.schedule-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.schedule-info { flex: 1; display: flex; gap: 8px; align-items: center; }
.schedule-name { color: var(--el-text-color-primary); }
.schedule-location { font-size: 12px; color: var(--el-text-color-secondary); }
.view-more {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-color-primary);
  text-decoration: none;
}
.week-grid { display: flex; gap: 4px; }
.week-col { flex: 1; min-width: 0; }
.week-col-header {
  text-align: center;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}
.week-course-block {
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 3px;
  border-left: 3px solid;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
```

- [ ] **Step 2: Create TodoWidget.vue**

Shows only uncompleted + today-due tasks.

```vue
<script setup lang="ts">
import { computed, ref } from 'vue'
import dayjs from 'dayjs'
import { useTodoStore } from '@/stores/todo'
import { ElMessage } from 'element-plus'

const store = useTodoStore()
const newContent = ref('')

const activeTodos = computed(() =>
  store.todos
    .filter(t => !t.done)
    .slice(0, 5)
)

async function addTodo() {
  const content = newContent.value.trim()
  if (!content) return
  await store.addTodo({ content, done: false, createdAt: new Date() })
  newContent.value = ''
}

async function toggleDone(todo: any) {
  await store.toggleDone(todo.id, todo.done)
}
</script>

<template>
  <div class="todo-widget">
    <div v-if="activeTodos.length === 0" class="empty">
      没有未完成的待办 🎉
    </div>
    <div
      v-for="todo in activeTodos"
      :key="todo.id"
      class="todo-item"
    >
      <el-checkbox :model-value="todo.done" @change="toggleDone(todo)" size="small" />
      <span class="todo-content">{{ todo.content }}</span>
      <span v-if="todo.dueDate?.toDate" class="todo-due">
        截止: {{ dayjs(todo.dueDate.toDate()).format('M/D') }}
      </span>
    </div>
    <div class="add-row">
      <el-input
        v-model="newContent"
        placeholder="添加待办..."
        size="small"
        @keyup.enter="addTodo"
      />
    </div>
    <router-link to="/todo" class="view-more">查看全部 →</router-link>
  </div>
</template>

<style scoped>
.todo-widget { font-size: 14px; }
.empty { color: var(--el-text-color-secondary); padding: 16px 0; text-align: center; font-size: 13px; }
.todo-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
}
.todo-content { flex: 1; }
.todo-due { font-size: 12px; color: var(--el-text-color-secondary); }
.add-row { margin-top: 8px; }
.view-more {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-color-primary);
  text-decoration: none;
}
</style>
```

- [ ] **Step 3: Create ImportantDatesWidget.vue**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import { useImportantDateStore } from '@/stores/importantDates'

const store = useImportantDateStore()

const upcoming = computed(() =>
  store.dates
    .filter(d => {
      const dt = d.date?.toDate ? d.date.toDate() : null
      return dt && dt >= new Date()
    })
    .sort((a, b) => {
      const da = a.date?.toDate ? a.date.toDate().getTime() : 0
      const db = b.date?.toDate ? b.date.toDate().getTime() : 0
      return da - db
    })
    .slice(0, 4)
)
</script>

<template>
  <div class="dates-widget">
    <div v-if="upcoming.length === 0" class="empty">
      暂无即将到来的日期
    </div>
    <div
      v-for="d in upcoming"
      :key="d.id"
      class="date-item"
    >
      <div class="date-color" :style="{ background: d.color }" />
      <div class="date-info">
        <span class="date-name">{{ d.name }}</span>
        <span class="date-label">
          {{ d.date?.toDate ? dayjs(d.date.toDate()).format('M月D日') : '' }}
        </span>
      </div>
      <span class="date-countdown">
        {{ d.date?.toDate ? dayjs(d.date.toDate()).diff(dayjs(), 'day') + 1 : '?' }} 天
      </span>
    </div>
    <router-link to="/dates" class="view-more">管理 →</router-link>
  </div>
</template>

<style scoped>
.dates-widget { font-size: 14px; }
.empty { color: var(--el-text-color-secondary); padding: 16px 0; text-align: center; font-size: 13px; }
.date-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}
.date-color {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.date-info { flex: 1; display: flex; flex-direction: column; gap: 1px; }
.date-name { font-size: 14px; color: var(--el-text-color-primary); }
.date-label { font-size: 12px; color: var(--el-text-color-secondary); }
.date-countdown { font-size: 13px; color: var(--el-color-primary); font-weight: 500; white-space: nowrap; }
.view-more {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-color-primary);
  text-decoration: none;
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "feat: add dashboard widgets (schedule, todo, important dates)"
```

---

### Task 15: Dashboard 主页

**Files:**
- Create: `daily-dashboard/src/views/DashboardView.vue`

- [ ] **Step 1: Create DashboardView.vue**

Layout: top overview bar + 2-column grid of widgets.

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { useWeatherStore } from '@/stores/weather'
import { useCourseStore } from '@/stores/course'
import { useTodoStore } from '@/stores/todo'
import { useCalendarStore } from '@/stores/calendar'
import { useImportantDateStore } from '@/stores/importantDates'
import { useCsgoStore } from '@/stores/csgo'
import { useSemesterStore } from '@/stores/semester'
import WeatherWidget from '@/components/widgets/WeatherWidget.vue'
import ScheduleWidget from '@/components/widgets/ScheduleWidget.vue'
import ImportantDatesWidget from '@/components/widgets/ImportantDatesWidget.vue'
import TodoWidget from '@/components/widgets/TodoWidget.vue'
import CsgoWidget from '@/components/widgets/CsgoWidget.vue'

const weather = useWeatherStore()
const courseStore = useCourseStore()
const todoStore = useTodoStore()
const calendarStore = useCalendarStore()
const dateStore = useImportantDateStore()
const csgoStore = useCsgoStore()
const semesterStore = useSemesterStore()

dayjs.locale('zh-cn')

onMounted(() => {
  todoStore.fetchTodos()
  dateStore.fetchDates()
  csgoStore.refresh()
  courseStore.fetchCourses()
  semesterStore.fetchSemester()
  calendarStore.fetchEvents()
})
</script>

<template>
  <div class="dashboard">
    <!-- Top Overview Bar -->
    <div class="overview-bar">
      <WeatherWidget />
      <div class="overview-right">
        <div class="date-display">{{ dayjs().format('YYYY年M月D日 dddd') }}</div>
        <div class="today-summary">
          {{ courseStore.courses.length ? `${courseStore.courses.length} 门课` : '' }}
          {{ todoStore.todos.filter(t => !t.done).length ? `· ${todoStore.todos.filter(t => !t.done).length} 项待办` : '' }}
        </div>
      </div>
    </div>

    <!-- Widget Grid -->
    <div class="widget-grid">
      <div class="widget-card card-schedule">
        <div class="widget-title">
          <span>📅 日程课表</span>
        </div>
        <ScheduleWidget />
      </div>

      <div class="widget-card card-dates">
        <div class="widget-title">
          <span>🎯 重大日期</span>
        </div>
        <ImportantDatesWidget />
      </div>

      <div class="widget-card card-todo">
        <div class="widget-title">
          <span>✅ 待办</span>
        </div>
        <TodoWidget />
      </div>

      <div class="widget-card card-csgo">
        <div class="widget-title">
          <span>🎮 CSGO 赛事</span>
        </div>
        <CsgoWidget />
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}
.overview-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--el-bg-color);
  border-radius: 12px;
  margin-bottom: 20px;
}
.overview-right { text-align: right; }
.date-display { font-size: 16px; font-weight: 600; color: var(--el-text-color-primary); }
.today-summary { font-size: 13px; color: var(--el-text-color-secondary); margin-top: 2px; }
.widget-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.widget-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 16px 20px;
}
.widget-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-light);
}

/* Responsive: single column on narrow screens */
@media (max-width: 768px) {
  .widget-grid {
    grid-template-columns: 1fr;
  }
  .overview-bar {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  .overview-right { text-align: left; }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add .
git commit -m "feat: add dashboard home page with widget grid"
```

---

### Phase 4 — 收尾

### Task 16: 设置页面 + 深色模式

**Files:**
- Create: `daily-dashboard/src/views/SettingsView.vue`

- [ ] **Step 1: Create SettingsView.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useWeatherStore } from '@/stores/weather'
import { useRouter } from 'vue-router'
import { doc, getDoc, setDoc } from 'firebase/firestore'
import { db } from '@/firebase/init'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const weatherStore = useWeatherStore()
const router = useRouter()

const city = ref('')
const darkMode = ref(false)

onMounted(async () => {
  city.value = weatherStore.city
  if (auth.user) {
    const snap = await getDoc(doc(db, 'users', auth.user.uid))
    if (snap.exists()) {
      darkMode.value = !!snap.data()?.settings?.darkMode
    }
  }
})

function applyDarkMode(val: boolean) {
  document.documentElement.classList.toggle('dark', val)
}

async function saveCity() {
  weatherStore.setCity(city.value)
  ElMessage.success('城市已更新')
}

async function toggleDarkMode(val: boolean) {
  darkMode.value = val
  applyDarkMode(val)
  if (auth.user) {
    await setDoc(doc(db, 'users', auth.user.uid), {
      settings: { darkMode: val },
    }, { merge: true })
  }
}

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="settings-view">
    <h2 class="page-title">设置</h2>

    <el-card class="settings-card">
      <h3>天气</h3>
      <div class="setting-row">
        <el-input v-model="city" placeholder="城市名称" style="width:200px" />
        <el-button type="primary" @click="saveCity">保存</el-button>
      </div>
    </el-card>

    <el-card class="settings-card">
      <h3>外观</h3>
      <div class="setting-row">
        <span>深色模式</span>
        <el-switch :model-value="darkMode" @update:model-value="toggleDarkMode" />
      </div>
    </el-card>

    <el-card class="settings-card">
      <h3>账户</h3>
      <p class="user-email">{{ auth.user?.email }}</p>
      <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.settings-view { max-width: 600px; }
.page-title { font-size: 20px; color: var(--el-text-color-primary); margin-bottom: 24px; }
.settings-card { margin-bottom: 16px; }
.settings-card h3 { font-size: 14px; margin-bottom: 12px; color: var(--el-text-color-primary); }
.setting-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-email { font-size: 14px; color: var(--el-text-color-secondary); margin-bottom: 12px; }
</style>
```

- [ ] **Step 2: Wire up dark mode in App.vue**

Add dark mode initialization to `src/App.vue`:

After the auth store init, add logic to read dark mode from Firestore:

```ts
// Add to App.vue <script>
import { onMounted } from 'vue'
import { doc, getDoc } from 'firebase/firestore'
import { db } from '@/firebase/init'

onMounted(async () => {
  if (auth.user) {
    const snap = await getDoc(doc(db, 'users', auth.user.uid))
    if (snap.exists()) {
      const dark = !!snap.data()?.settings?.darkMode
      document.documentElement.classList.toggle('dark', dark)
    }
  }
})
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "feat: add settings page with dark mode toggle"
```

---

### Task 17: 响应式优化 + 最终验证

- [ ] **Step 1: Verify dev server starts without errors**

Run: `cd d:\code\claude\daily-dashboard && npx vite --port 5174`
Expected: Vite dev server starts on port 5174

- [ ] **Step 2: Verify build succeeds**

Run: `cd d:\code\claude\daily-dashboard && npx vue-tsc -b`
Expected: No type errors

Run: `cd d:\code\claude\daily-dashboard && npx vite build`
Expected: Build success

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "chore: finalize project with responsive polish"
```
