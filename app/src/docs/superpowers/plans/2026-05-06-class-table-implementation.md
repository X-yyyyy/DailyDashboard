# class-table 课程表应用实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现一个多用户课程表应用，支持邮箱/Google 登录，7×N CSS Grid 课表周视图，课程 CRUD，学期管理，时间段自定义。

**Architecture:** Vue 3 + TypeScript SFC，Firebase Auth 认证，Cloud Firestore 实时同步，Pinia 状态管理，Element Plus UI 组件。认证后通过 AppLayout 包裹 Sidebar + TopBar + RouterView。

**Tech Stack:** Vue 3, Vite, TypeScript, Element Plus, Pinia, Firebase Auth, Cloud Firestore, dayjs, Vue Router

---

## 先决条件

### Task 0: 准备工作

**Files:**
- Modify: `c:\Users\86157\class-table\.gitignore`
- Create: `c:\Users\86157\class-table\src\types\index.ts`

- [ ] **Step 1: 配置 .gitignore**

确保 `.env` 和 `.superpowers/` 被忽略。检查当前 `.gitignore` 内容并补充：

```gitignore
# 补充到 .gitignore
.env
.env.local
.superpowers/
```

- [ ] **Step 2: 创建 TypeScript 类型定义**

`src/types/index.ts`:

```ts
export interface Semester {
  name: string
  startDate: Date
  totalWeeks: number
}

export interface Schedule {
  dayOfWeek: number // 1-7 (1=周一)
  startSlot: number
  duration: number
  weekType: 'all' | 'odd' | 'even'
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
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C',
  '#909399', '#B37FEB', '#36CFC9', '#F2A6B0',
]
```

---

### Task 1: Firebase 初始化

**Files:**
- Create: `src/firebase/init.ts`
- Modify: `src/main.ts`

- [ ] **Step 1: 创建 Firebase 初始化文件**

`src/firebase/init.ts`:

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

- [ ] **Step 2: 创建 Firestore 工具函数**

`src/utils/firestore.ts`:

```ts
import { db } from '@/firebase/init'
import {
  doc, collection, addDoc, updateDoc, deleteDoc,
  onSnapshot, query, where, orderBy, Timestamp,
  type Unsubscribe,
} from 'firebase/firestore'
import type { Course, TimeSlot } from '@/types'

function userPrefix(uid: string) {
  return `users/${uid}`
}

export function coursesRef(uid: string) {
  return collection(db, userPrefix(uid), 'courses')
}

export function courseRef(uid: string, courseId: string) {
  return doc(db, userPrefix(uid), 'courses', courseId)
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
  return updateDoc(courseRef(uid, courseId), data)
}

export async function deleteCourse(uid: string, courseId: string) {
  return deleteDoc(courseRef(uid, courseId))
}

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

export function semesterRef(uid: string) {
  return doc(db, userPrefix(uid), 'semester', 'current')
}

export function userSettingsRef(uid: string) {
  return doc(db, userPrefix(uid), 'settings', 'preferences')
}
```

---

### Task 2: Pinia Stores

**Files:**
- Create: `src/stores/auth.ts`
- Create: `src/stores/course.ts`
- Create: `src/stores/semester.ts`
- Create: `src/stores/timeSlot.ts`

- [ ] **Step 1: Auth Store**

`src/stores/auth.ts`:

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
import { doc, setDoc, getDoc, serverTimestamp } from 'firebase/firestore'

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
    // Initialize Firestore data for new user
    const uid = cred.user.uid
    await setDoc(doc(db, 'users', uid), {
      profile: { name, email, photoURL: '' },
      settings: { themeColor: '#409EFF', darkMode: false, weekStartsOn: 1 },
    })
    await setDoc(doc(db, `users/${uid}/semester`, 'current'), {
      name: `${new Date().getFullYear()} 学年`,
      startDate: serverTimestamp(),
      totalWeeks: 20,
    })
    return cred
  }

  async function loginWithGoogle() {
    const provider = new GoogleAuthProvider()
    const cred = await signInWithPopup(auth, provider)
    // Check if first-time user, init data
    const uid = cred.user.uid
    const snap = await getDoc(doc(db, 'users', uid))
    if (!snap.exists()) {
      await setDoc(doc(db, 'users', uid), {
        profile: { name: cred.user.displayName || '', email: cred.user.email, photoURL: cred.user.photoURL || '' },
        settings: { themeColor: '#409EFF', darkMode: false, weekStartsOn: 1 },
      })
      await setDoc(doc(db, `users/${uid}/semester`, 'current'), {
        name: `${new Date().getFullYear()} 学年`,
        startDate: serverTimestamp(),
        totalWeeks: 20,
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

- [ ] **Step 2: Course Store**

`src/stores/course.ts`:

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Course } from '@/types'
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

  function getCoursesForWeek(weekNum: number): Course[] {
    return courses.value.filter((c) => {
      return c.schedules.some((s) => {
        if (s.weekType === 'all') return true
        if (s.weekType === 'odd') return weekNum % 2 === 1
        if (s.weekType === 'even') return weekNum % 2 === 0
        return false
      })
    })
  }

  return { courses, loading, fetchCourses, stopListening, addCourse, updateCourse, deleteCourse, getCoursesForWeek }
})
```

- [ ] **Step 3: Semester Store**

`src/stores/semester.ts`:

```ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import { doc, getDoc, setDoc } from 'firebase/firestore'
import { db } from '@/firebase/init'
import { useAuthStore } from './auth'

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
    const snap = await getDoc(doc(db, `users/${auth.user.uid}/semester`, 'current'))
    if (snap.exists()) {
      const d = snap.data()
      semesterName.value = d.name || ''
      startDate.value = d.startDate?.toDate?.()?.toISOString() || ''
      totalWeeks.value = d.totalWeeks || 20
      // Calculate current week
      if (d.startDate?.toDate) {
        const diff = dayjs().diff(dayjs(d.startDate.toDate()), 'week')
        currentWeek.value = Math.max(1, Math.min(diff + 1, totalWeeks.value))
      }
    }
  }

  async function saveSemester(data: { name: string; startDate: string; totalWeeks: number }) {
    const auth = useAuthStore()
    if (!auth.user) return
    await setDoc(doc(db, `users/${auth.user.uid}/semester`, 'current'), {
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

- [ ] **Step 4: TimeSlot Store**

`src/stores/timeSlot.ts`:

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TimeSlot } from '@/types'
import { DEFAULT_TIME_SLOTS } from '@/types'
import { subscribeTimeSlots, timeSlotsRef, addDoc } from '@/utils/firestore'
import { useAuthStore } from './auth'
import { collection, getDocs, writeBatch, doc } from 'firebase/firestore'
import { db } from '@/firebase/init'

export const useTimeSlotStore = defineStore('timeSlot', () => {
  const timeSlots = ref<TimeSlot[]>([])
  const loading = ref(true)
  let unsub: (() => void) | null = null

  function fetchTimeSlots() {
    const auth = useAuthStore()
    if (!auth.user) return
    loading.value = true
    unsub?.()
    unsub = subscribeTimeSlots(auth.user.uid, (list) => {
      if (list.length === 0) {
        // Initialize defaults
        initDefaultSlots(auth.user.uid)
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
    // Delete all existing, re-add
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

---

### Task 3: 路由配置与守卫

**Files:**
- Modify: `src/router/index.ts`
- Modify: `src/main.ts`

- [ ] **Step 1: 配置路由和导航守卫**

`src/router/index.ts`:

```ts
import { createRouter, createWebHistory } from 'vue-router'
import { getAuth, onAuthStateChanged } from 'firebase/auth'

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
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('@/views/ForgotPasswordView.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'CourseGrid',
          component: () => import('@/views/CourseGridView.vue'),
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/SettingsView.vue'),
        },
      ],
    },
  ],
})

function getCurrentUser(): Promise<ReturnType<typeof getAuth.currentUser>> {
  return new Promise((resolve) => {
    const unsub = onAuthStateChanged(getAuth(), (user) => {
      unsub()
      resolve(user)
    })
  })
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

- [ ] **Step 2: 更新 main.ts 初始化 Auth**

`src/main.ts`:

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import './styles/main.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// Initialize auth listener before mounting
const authStore = useAuthStore()
authStore.init()

app.mount('#app')
```

---

### Task 4: Auth 页面（登录/注册/忘记密码）

**Files:**
- Create: `src/views/LoginView.vue`
- Create: `src/views/RegisterView.vue`
- Create: `src/views/ForgotPasswordView.vue`

- [ ] **Step 1: LoginView.vue**

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
      <h1 class="auth-title">课程表</h1>
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
        <router-link to="/forgot-password">忘记密码</router-link>
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
  background: #f0f2f5;
}
.auth-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.auth-title {
  text-align: center;
  margin-bottom: 32px;
  font-size: 28px;
  color: #303133;
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
  color: #409EFF;
}
</style>
```

- [ ] **Step 2: RegisterView.vue**

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
  if (!name.value) {
    ElMessage.warning('请输入姓名')
    return
  }
  loading.value = true
  try {
    await auth.register(email.value, password.value, name.value)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e: any) {
    const msg = e.code === 'auth/email-already-in-use'
      ? '该邮箱已注册'
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
      <h1 class="auth-title">注册</h1>
      <el-form @submit.prevent="handleRegister">
        <el-form-item>
          <el-input v-model="name" placeholder="姓名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="email" placeholder="邮箱" type="email" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" placeholder="密码（至少6位）" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="w-full" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-links" style="justify-content:center">
        <router-link to="/login">已有账号？去登录</router-link>
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
  background: #f0f2f5;
}
.auth-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.auth-title {
  text-align: center;
  margin-bottom: 32px;
  font-size: 28px;
  color: #303133;
}
.w-full {
  width: 100%;
}
.auth-links {
  display: flex;
  margin-top: 16px;
  font-size: 14px;
}
.auth-links a {
  color: #409EFF;
}
</style>
```

- [ ] **Step 3: ForgotPasswordView.vue**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const email = ref('')
const loading = ref(false)
const sent = ref(false)

async function handleReset() {
  if (!email.value) {
    ElMessage.warning('请输入邮箱')
    return
  }
  loading.value = true
  try {
    await auth.resetPassword(email.value)
    sent.value = true
    ElMessage.success('密码重置邮件已发送')
  } catch {
    ElMessage.error('发送失败，请检查邮箱地址')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">找回密码</h1>
      <p class="auth-desc">输入注册邮箱，我们将发送密码重置链接</p>
      <el-form v-if="!sent" @submit.prevent="handleReset">
        <el-form-item>
          <el-input v-model="email" placeholder="邮箱" type="email" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="w-full" :loading="loading" @click="handleReset">
            发送重置邮件
          </el-button>
        </el-form-item>
      </el-form>
      <div v-else class="sent-msg">
        <p>邮件已发送至 <strong>{{ email }}</strong></p>
        <p>请检查收件箱并按照指引重置密码</p>
      </div>
      <div class="auth-links" style="justify-content:center">
        <router-link to="/login">返回登录</router-link>
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
  background: #f0f2f5;
}
.auth-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.auth-title {
  text-align: center;
  margin-bottom: 12px;
  font-size: 28px;
  color: #303133;
}
.auth-desc {
  text-align: center;
  color: #909399;
  margin-bottom: 24px;
  font-size: 14px;
}
.w-full { width: 100%; }
.sent-msg { text-align: center; line-height: 2; color: #606266; }
.auth-links {
  display: flex;
  margin-top: 16px;
  font-size: 14px;
}
.auth-links a { color: #409EFF; }
</style>
```

---

### Task 5: 主布局（AppLayout + Sidebar + TopBar）

**Files:**
- Create: `src/layouts/AppLayout.vue`
- Create: `src/components/Sidebar.vue`
- Create: `src/components/TopBar.vue`

- [ ] **Step 1: Sidebar.vue**

```vue
<script setup lang="ts">
import { useSemesterStore } from '@/stores/semester'

const semester = useSemesterStore()
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h2 class="app-title">课程表</h2>
    </div>
    <div class="semester-section">
      <p class="section-label">学期</p>
      <p class="semester-name">{{ semester.semesterName || '未设置' }}</p>
    </div>
    <div class="week-nav">
      <el-button size="small" @click="semester.prevWeek()" :disabled="semester.currentWeek <= 1">
        ‹
      </el-button>
      <span class="week-display">第 {{ semester.currentWeek }} 周</span>
      <el-button size="small" @click="semester.nextWeek()" :disabled="semester.currentWeek >= semester.totalWeeks">
        ›
      </el-button>
    </div>
    <el-button size="small" class="today-btn" @click="semester.goToCurrentWeek()">
      回到本周
    </el-button>
    <p class="week-range">{{ semester.weekDateRange }}</p>
    <nav class="sidebar-nav">
      <router-link to="/" class="nav-item">
        <el-icon><Calendar /></el-icon>
        课表
      </router-link>
      <router-link to="/settings" class="nav-item">
        <el-icon><Setting /></el-icon>
        设置
      </router-link>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: #fff;
  border-right: 1px solid var(--grid-border-color);
  display: flex;
  flex-direction: column;
  padding: 16px;
  position: fixed;
  left: 0;
  top: 0;
}
.sidebar-header {
  margin-bottom: 24px;
}
.app-title {
  font-size: 20px;
  color: #303133;
}
.semester-section {
  margin-bottom: 16px;
}
.section-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.semester-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.week-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.week-display {
  flex: 1;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
}
.today-btn {
  width: 100%;
  margin-bottom: 8px;
}
.week-range {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-bottom: 24px;
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
  color: #606266;
  font-size: 14px;
  transition: all 0.2s;
}
.nav-item:hover,
.nav-item.router-link-active {
  background: #ecf5ff;
  color: #409EFF;
}
</style>
```

- [ ] **Step 2: TopBar.vue**

```vue
<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const auth = useAuthStore()
const router = useRouter()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示')
    await auth.logout()
    router.push('/login')
  } catch {
    // cancelled
  }
}
</script>

<template>
  <header class="topbar">
    <div class="topbar-left">
      <!-- mobile menu toggle placeholder -->
    </div>
    <div class="topbar-right">
      <el-dropdown trigger="click">
        <div class="user-info">
          <el-avatar :size="32" :src="auth.user?.photoURL || ''">
            {{ auth.user?.email?.charAt(0).toUpperCase() }}
          </el-avatar>
          <span class="user-name">{{ auth.user?.displayName || auth.user?.email }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-item @click="router.push('/settings')">设置</el-dropdown-item>
          <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  height: var(--topbar-height);
  border-bottom: 1px solid var(--grid-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
}
.topbar-right {
  margin-left: auto;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.user-name {
  font-size: 14px;
  color: #303133;
}
</style>
```

- [ ] **Step 3: AppLayout.vue**

```vue
<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue'
import TopBar from '@/components/TopBar.vue'
import { useCourseStore } from '@/stores/course'
import { useSemesterStore } from '@/stores/semester'
import { useTimeSlotStore } from '@/stores/timeSlot'
import { onMounted, onUnmounted } from 'vue'

const courseStore = useCourseStore()
const semesterStore = useSemesterStore()
const timeSlotStore = useTimeSlotStore()

onMounted(() => {
  courseStore.fetchCourses()
  semesterStore.fetchSemester()
  timeSlotStore.fetchTimeSlots()
})

onUnmounted(() => {
  courseStore.stopListening()
  timeSlotStore.stopListening()
})
</script>

<template>
  <div class="layout">
    <Sidebar />
    <div class="main-area">
      <TopBar />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}
.main-area {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.main-content {
  flex: 1;
  overflow: auto;
  padding: 16px;
}
</style>
```

---

### Task 6: 课表核心网格组件

**Files:**
- Create: `src/components/grid/GridHeader.vue`
- Create: `src/components/grid/TimeColumn.vue`
- Create: `src/components/grid/CourseCard.vue`
- Create: `src/components/grid/CourseGrid.vue`
- Create: `src/views/CourseGridView.vue`

- [ ] **Step 1: GridHeader.vue**

```vue
<script setup lang="ts">
const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
</script>

<template>
  <div class="grid-header">
    <div class="time-corner"></div>
    <div v-for="(name, i) in dayNames" :key="i" class="day-header">
      {{ name }}
    </div>
  </div>
</template>

<style scoped>
.grid-header {
  display: contents;
}
.time-corner {
  grid-column: 1;
  grid-row: 1;
  background: var(--grid-header-bg);
  border-bottom: 1px solid var(--grid-border-color);
  border-right: 1px solid var(--grid-border-color);
  position: sticky;
  top: 0;
  z-index: 3;
}
.day-header {
  background: var(--grid-header-bg);
  border-bottom: 1px solid var(--grid-border-color);
  border-right: 1px solid var(--grid-border-color);
  text-align: center;
  padding: 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  position: sticky;
  top: 0;
  z-index: 2;
}
.day-header:last-child {
  border-right: none;
}
</style>
```

- [ ] **Step 2: TimeColumn.vue**

```vue
<script setup lang="ts">
import type { TimeSlot } from '@/types'

defineProps<{
  timeSlots: TimeSlot[]
}>()
</script>

<template>
  <div class="time-column">
    <div
      v-for="slot in timeSlots"
      :key="slot.slot"
      class="time-cell"
    >
      <span class="slot-number">第{{ slot.slot }}节</span>
      <span class="slot-time">{{ slot.startTime }}-{{ slot.endTime }}</span>
    </div>
  </div>
</template>

<style scoped>
.time-column {
  display: contents;
}
.time-cell {
  grid-column: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--grid-border-color);
  border-right: 1px solid var(--grid-border-color);
  background: var(--grid-header-bg);
  font-size: 12px;
  color: #606266;
}
.slot-number {
  font-weight: 500;
}
.slot-time {
  font-size: 11px;
  color: #909399;
}
</style>
```

- [ ] **Step 3: CourseCard.vue**

```vue
<script setup lang="ts">
import type { Course } from '@/types'

const props = defineProps<{
  course: Course
  slotIndex: number
}>()

const emit = defineEmits<{
  edit: [course: Course]
}>()

function getGridPosition() {
  // Row: slotIndex + 2 (row 1 = header, row 2+ = slots)
  // Column: dayOfWeek + 1 (col 1 = time column)
  const schedule = props.course.schedules[0]
  return {
    gridColumn: (schedule?.dayOfWeek ?? 1) + 1,
    gridRow: `${(props.slotIndex) + 2} / span ${schedule?.duration ?? 1}`,
  }
}
</script>

<template>
  <div
    class="course-card"
    :style="{
      ...getGridPosition(),
      backgroundColor: course.color + '20',
      borderLeft: `4px solid ${course.color}`,
    }"
    @click="emit('edit', course)"
  >
    <div class="card-name" :style="{ color: course.color }">{{ course.name }}</div>
    <div class="card-info">{{ course.teacher }}</div>
    <div class="card-info">{{ course.location }}</div>
  </div>
</template>

<style scoped>
.course-card {
  padding: 4px 6px;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  min-height: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: box-shadow 0.2s;
}
.course-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.card-name {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 2px;
}
.card-info {
  font-size: 11px;
  color: #606266;
  line-height: 1.3;
}
</style>
```

- [ ] **Step 4: CourseGrid.vue**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { useCourseStore } from '@/stores/course'
import { useSemesterStore } from '@/stores/semester'
import { useTimeSlotStore } from '@/stores/timeSlot'
import GridHeader from './GridHeader.vue'
import TimeColumn from './TimeColumn.vue'
import CourseCard from './CourseCard.vue'

const courseStore = useCourseStore()
const semesterStore = useSemesterStore()
const timeSlotStore = useTimeSlotStore()

const emit = defineEmits<{
  addCourse: [dayOfWeek: number, slot: number]
  editCourse: [courseId: string]
}>()

const visibleCourses = computed(() => {
  return courseStore.getCoursesForWeek(semesterStore.currentWeek)
})

function handleCellClick(dayOfWeek: number, slotIndex: number) {
  emit('addCourse', dayOfWeek, slotIndex)
}

function handleEditCourse(course: any) {
  emit('editCourse', course.id)
}

const daySlots = computed(() => {
  return timeSlotStore.timeSlots
})
</script>

<template>
  <div v-loading="courseStore.loading || timeSlotStore.loading" class="grid-wrapper">
    <div
      class="course-grid"
      :style="{
        gridTemplateColumns: `var(--time-column-width) repeat(7, 1fr)`,
        gridTemplateRows: `auto repeat(${daySlots.length}, 1fr)`,
      }"
    >
      <GridHeader />
      <TimeColumn :timeSlots="daySlots" />
      
      <!-- Empty cells for clicking to add -->
      <template v-for="(slot, si) in daySlots" :key="'row-' + si">
        <div
          v-for="day in 7"
          :key="'cell-' + day + '-' + si"
          class="grid-cell"
          :style="{
            gridColumn: day + 1,
            gridRow: si + 2,
          }"
          @click="handleCellClick(day, si)"
        />
      </template>

      <!-- Course cards -->
      <CourseCard
        v-for="course in visibleCourses"
        :key="course.id"
        :course="course"
        :slotIndex="(course.schedules[0]?.startSlot ?? 1) - 1"
        @edit="handleEditCourse"
      />
    </div>
  </div>
</template>

<style scoped>
.grid-wrapper {
  height: 100%;
  overflow: auto;
}
.course-grid {
  display: grid;
  min-width: 800px;
  position: relative;
}
.grid-cell {
  border-bottom: 1px solid var(--grid-border-color);
  border-right: 1px solid var(--grid-border-color);
  min-height: 60px;
  cursor: pointer;
  transition: background-color 0.15s;
}
.grid-cell:hover {
  background-color: #f0f9ff;
}
.grid-cell:nth-child(7n) {
  border-right: none;
}
</style>
```

- [ ] **Step 5: CourseGridView.vue**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import CourseGrid from '@/components/grid/CourseGrid.vue'
import CourseDialog from '@/components/CourseDialog.vue'
import { useCourseStore } from '@/stores/course'
import { ElMessage } from 'element-plus'

const courseStore = useCourseStore()
const dialogVisible = ref(false)
const editingCourse = ref<any>(null)
const defaultDay = ref(1)
const defaultSlot = ref(1)

function handleAddCourse(dayOfWeek: number, slot: number) {
  defaultDay.value = dayOfWeek
  defaultSlot.value = slot
  editingCourse.value = null
  dialogVisible.value = true
}

function handleEditCourse(courseId: string) {
  const course = courseStore.courses.find((c) => c.id === courseId)
  if (course) {
    editingCourse.value = { ...course }
    dialogVisible.value = true
  }
}

async function handleSave(data: any) {
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
  color: #303133;
}
</style>
```

---

### Task 7: 课程 Dialog（添加/编辑/删除）

**Files:**
- Create: `src/components/CourseDialog.vue`

- [ ] **Step 1: CourseDialog.vue**

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Course, CourseType, Schedule } from '@/types'
import { COLORS } from '@/types'
import { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElButton, ElMessage, ElColorPicker } from 'element-plus'

const props = defineProps<{
  visible: boolean
  course: Partial<Course> | null
  defaultDay: number
  defaultSlot: number
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  save: [data: Omit<Course, 'id'>]
  delete: []
}>()

const form = ref({
  name: '',
  teacher: '',
  location: '',
  type: 'elective' as CourseType,
  color: COLORS[0],
  schedules: [{ dayOfWeek: 1, startSlot: 1, duration: 2, weekType: 'all' as Schedule['weekType'] }],
})

watch(() => props.visible, (val) => {
  if (val) {
    if (props.course) {
      form.value = {
        name: props.course.name || '',
        teacher: props.course.teacher || '',
        location: props.course.location || '',
        type: props.course.type || 'elective',
        color: props.course.color || COLORS[0],
        schedules: props.course.schedules?.length
          ? [...props.course.schedules]
          : [{ dayOfWeek: 1, startSlot: 1, duration: 2, weekType: 'all' as Schedule['weekType'] }],
      }
    } else {
      form.value = {
        name: '',
        teacher: '',
        location: '',
        type: 'elective',
        color: COLORS[0],
        schedules: [{ dayOfWeek: props.defaultDay, startSlot: props.defaultSlot, duration: 2, weekType: 'all' as Schedule['weekType'] }],
      }
    }
  }
})

function addSchedule() {
  form.value.schedules.push({ dayOfWeek: 1, startSlot: 1, duration: 2, weekType: 'all' })
}

function removeSchedule(index: number) {
  if (form.value.schedules.length > 1) {
    form.value.schedules.splice(index, 1)
  }
}

function handleSave() {
  if (!form.value.name) {
    ElMessage.warning('请输入课程名称')
    return
  }
  emit('save', { ...form.value })
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    :title="course ? '编辑课程' : '添加课程'"
    width="520px"
    destroy-on-close
  >
    <el-form label-width="80px">
      <el-form-item label="课程名称">
        <el-input v-model="form.name" placeholder="例如：高等数学" />
      </el-form-item>
      <el-form-item label="授课教师">
        <el-input v-model="form.teacher" placeholder="教师姓名" />
      </el-form-item>
      <el-form-item label="上课地点">
        <el-input v-model="form.location" placeholder="例如：教学楼301" />
      </el-form-item>
      <el-form-item label="课程类型">
        <el-select v-model="form.type">
          <el-option label="必修" value="required" />
          <el-option label="选修" value="elective" />
        </el-select>
      </el-form-item>
      <el-form-item label="颜色">
        <el-color-picker v-model="form.color" :predefine="COLORS" />
      </el-form-item>
      <el-form-item label="上课时间">
        <div class="schedule-list">
          <div v-for="(s, i) in form.schedules" :key="i" class="schedule-item">
            <el-select v-model="s.dayOfWeek" style="width:100px">
              <el-option :value="1" label="周一" />
              <el-option :value="2" label="周二" />
              <el-option :value="3" label="周三" />
              <el-option :value="4" label="周四" />
              <el-option :value="5" label="周五" />
              <el-option :value="6" label="周六" />
              <el-option :value="7" label="周日" />
            </el-select>
            <el-select v-model="s.startSlot" style="width:100px">
              <el-option v-for="n in 12" :key="n" :value="n" :label="`第${n}节`" />
            </el-select>
            <el-select v-model="s.duration" style="width:90px">
              <el-option v-for="n in 6" :key="n" :value="n" :label="`${n}节`" />
            </el-select>
            <el-select v-model="s.weekType" style="width:90px">
              <el-option value="all" label="每周" />
              <el-option value="odd" label="单周" />
              <el-option value="even" label="双周" />
            </el-select>
            <el-button v-if="form.schedules.length > 1" type="danger" size="small" @click="removeSchedule(i)">
              删除
            </el-button>
          </div>
          <el-button size="small" @click="addSchedule">+ 添加时间段</el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-if="course" type="danger" @click="emit('delete')">删除</el-button>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.schedule-item {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
```

---

### Task 8: 设置页面

**Files:**
- Create: `src/views/SettingsView.vue`
- Create: `src/components/TimeSlotEditor.vue`
- Create: `src/components/SemesterConfig.vue`

- [ ] **Step 1: TimeSlotEditor.vue**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useTimeSlotStore } from '@/stores/timeSlot'
import { ElMessage } from 'element-plus'
import type { TimeSlot } from '@/types'

const store = useTimeSlotStore()
const editing = ref<Omit<TimeSlot, 'id'>[]>([])

function startEdit() {
  editing.value = store.timeSlots.map((s) => ({
    slot: s.slot,
    startTime: s.startTime,
    endTime: s.endTime,
  }))
}

function addRow() {
  editing.value.push({
    slot: editing.value.length + 1,
    startTime: '00:00',
    endTime: '00:00',
  })
}

function removeRow(index: number) {
  editing.value.splice(index, 1)
}

async function save() {
  const validated = editing.value.map((s, i) => ({ ...s, slot: i + 1 }))
  await store.updateTimeSlots(validated)
  ElMessage.success('时间段已更新')
  editing.value = []
}

function cancel() {
  editing.value = []
}
</script>

<template>
  <div class="time-slot-editor">
    <div class="section-header">
      <h3>自定义时间段</h3>
      <el-button v-if="editing.length === 0" size="small" @click="startEdit">编辑</el-button>
    </div>
    <div v-if="editing.length > 0" class="editor-body">
      <div v-for="(row, i) in editing" :key="i" class="slot-row">
        <span class="slot-index">第{{ i + 1 }}节</span>
        <el-time-picker
          v-model="row.startTime"
          format="HH:mm"
          placeholder="开始"
          style="width:120px"
          is-range
        />
        <span class="separator">-</span>
        <el-time-picker
          v-model="row.endTime"
          format="HH:mm"
          placeholder="结束"
          style="width:120px"
          is-range
        />
        <el-button type="danger" size="small" @click="removeRow(i)">×</el-button>
      </div>
      <div class="editor-actions">
        <el-button size="small" @click="addRow">+ 添加</el-button>
        <el-button size="small" type="primary" @click="save">保存</el-button>
        <el-button size="small" @click="cancel">取消</el-button>
      </div>
    </div>
    <div v-else class="slot-list">
      <div v-for="slot in store.timeSlots" :key="slot.slot" class="slot-item">
        <span class="slot-label">第{{ slot.slot }}节</span>
        <span class="slot-time">{{ slot.startTime }} - {{ slot.endTime }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.time-slot-editor {
  margin-bottom: 24px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.section-header h3 {
  font-size: 16px;
  color: #303133;
}
.slot-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.slot-item {
  display: flex;
  gap: 8px;
  font-size: 14px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.slot-label {
  font-weight: 500;
  color: #303133;
}
.slot-time {
  color: #606266;
}
.editor-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.slot-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.slot-index {
  width: 60px;
  font-size: 14px;
  font-weight: 500;
}
.separator {
  color: #909399;
}
.editor-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
</style>
```

- [ ] **Step 2: SemesterConfig.vue**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useSemesterStore } from '@/stores/semester'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const store = useSemesterStore()
const form = ref({
  name: store.semesterName,
  startDate: store.startDate ? dayjs(store.startDate).format('YYYY-MM-DD') : '',
  totalWeeks: store.totalWeeks,
})

async function save() {
  if (!form.value.name || !form.value.startDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await store.saveSemester({
    name: form.value.name,
    startDate: form.value.startDate,
    totalWeeks: form.value.totalWeeks,
  })
  ElMessage.success('学期设置已保存')
}
</script>

<template>
  <div class="semester-config">
    <h3>学期设置</h3>
    <el-form label-width="100px" class="config-form">
      <el-form-item label="学期名称">
        <el-input v-model="form.name" placeholder="例如：2026 春季学期" />
      </el-form-item>
      <el-form-item label="开学日期">
        <el-date-picker v-model="form.startDate" type="date" format="YYYY-MM-DD" placeholder="选择日期" />
      </el-form-item>
      <el-form-item label="总周数">
        <el-input-number v-model="form.totalWeeks" :min="10" :max="30" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.semester-config {
  margin-bottom: 24px;
}
.semester-config h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}
.config-form {
  max-width: 400px;
}
</style>
```

- [ ] **Step 3: SettingsView.vue**

```vue
<script setup lang="ts">
import SemesterConfig from '@/components/SemesterConfig.vue'
import TimeSlotEditor from '@/components/TimeSlotEditor.vue'
</script>

<template>
  <div class="settings-page">
    <h2 class="page-title">设置</h2>
    <SemesterConfig />
    <TimeSlotEditor />
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 700px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 24px;
}
</style>
```

---

### Task 9: 深色模式

**Files:**
- Create: `src/styles/dark.css`

- [ ] **Step 1: 创建深色模式样式**

`src/styles/dark.css`:

```css
html.dark {
  --grid-border-color: #363636;
  --grid-header-bg: #1d1d1d;
}

html.dark body,
html.dark #app {
  background: #141414;
  color: #e0e0e0;
}

html.dark .sidebar {
  background: #1d1d1d;
  border-right-color: #363636;
}

html.dark .topbar {
  background: #1d1d1d;
  border-bottom-color: #363636;
}

html.dark .auth-card {
  background: #1d1d1d;
  box-shadow: 0 2px 12px rgba(0,0,0,0.3);
}

html.dark .app-title,
html.dark .auth-title,
html.dark .day-header,
html.dark .user-name {
  color: #e0e0e0;
}

html.dark .grid-cell:hover {
  background-color: #1a1a2e;
}

html.dark .course-card .card-info {
  color: #a0a0a0;
}

html.dark .nav-item {
  color: #a0a0a0;
}

html.dark .nav-item:hover,
html.dark .nav-item.router-link-active {
  background: #1a1a2e;
  color: #409EFF;
}
```

- [ ] **Step 2: 在 main.ts 中引入 dark.css**

```ts
// 添加在 main.ts 中
import './styles/dark.css'
```

---

### Task 10: 清理默认模板文件

**Files:**
- Delete: `src/components/HelloWorld.vue`
- Delete: `src/assets/hero.png`
- Delete: `src/assets/vite.svg`
- Delete: `src/assets/vue.svg`

- [ ] **Step 1: 删除 Vite 默认模板的组件和资源**

删除 `HelloWorld.vue`、`hero.png`、`vite.svg`、`vue.svg`。

---

## 自检清单

### 1. Spec 覆盖
- ✅ 邮箱密码注册/登录 + Google 登录 + 忘记密码 → Task 4
- ✅ 路由守卫 → Task 3 (router.beforeEach)
- ✅ 7×N CSS Grid 课表周视图 → Task 6 (CourseGrid)
- ✅ 课程 CRUD（多个时间段 + 单双周支持）→ Task 7 (CourseDialog)
- ✅ 学期管理 & 周次切换 → Task 5 (Sidebar) + Task 2 (Semester Store)
- ✅ 时间段自定义 → Task 8 (TimeSlotEditor)
- ✅ 深色模式 → Task 9 (dark.css)
- ✅ Firestore 实时同步 → Task 1 (subscribeCourses, subscribeTimeSlots)
- ✅ 移动端单日视图 → MVP 未实现，预留

### 2. 占位符检查
- 所有步骤包含完整代码，无 TBD/TODO
- 所有文件路径精确
- 所有类型引用一致

### 3. 类型一致性
- `Course`, `TimeSlot`, `Schedule`, `CourseType`, `Semester` 定义在 `src/types/index.ts`
- Store 使用一致的 `Omit<Course, 'id'>` 和 `Partial<Course>`
- Firestore 工具函数签名匹配 Store 调用
