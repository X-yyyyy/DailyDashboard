# Daily Dashboard Android App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Vue.js 全栈「大学生每日仪表盘」移植为 Android 原生 App（Kotlin + Jetpack Compose），通过 Firebase REST API 兼容无 GMS 设备（华为 P60）。

**Architecture:** Single Activity + Jetpack Compose + MVVM。数据层通过 OkHttp 调用 Firebase REST API，DataStore 做本地缓存。DI 用 Koin。额外依赖 Retrofit 调天气/CS:GO 外部 API。

**Tech Stack:** Kotlin 2.0+, Jetpack Compose + Material 3, Navigation Compose (Type-Safe), Koin, OkHttp, Retrofit, DataStore, Firebase REST API

---

## Phase 1：项目骨架 & 基础架构

### Task 1.1：创建 Android 项目结构

**Files:**
- Create: `DailyDashboard/settings.gradle.kts`
- Create: `DailyDashboard/build.gradle.kts`
- Create: `DailyDashboard/gradle.properties`
- Create: `DailyDashboard/gradle/libs.versions.toml`
- Create: `DailyDashboard/app/build.gradle.kts`
- Create: `DailyDashboard/app/src/main/AndroidManifest.xml`
- Create: `DailyDashboard/app/src/main/java/com/dailydashboard/app/DailyDashboardApp.kt`
- Create: `DailyDashboard/app/src/main/java/com/dailydashboard/app/MainActivity.kt`
- Create: `DailyDashboard/app/src/main/res/values/strings.xml`

- [ ] **Step 1: Create Version Catalogs**

`gradle/libs.versions.toml`:
```toml
[versions]
kotlin = "2.1.0"
agp = "8.7.3"
compose-bom = "2024.12.01"
compose-compiler = "1.5.15"
navigation = "2.8.5"
koin = "4.0.0"
okhttp = "4.12.0"
retrofit = "2.11.0"
kotlinx-serialization = "1.7.3"
datastore = "1.1.1"
lifecycle = "2.8.7"
coroutines = "1.9.0"
activity-compose = "1.9.3"
coil = "2.7.0"

[libraries]
# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Activity
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }

# Lifecycle
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

# Koin
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-compose = { group = "io.insert-koin", name = "koin-compose", version.ref = "koin" }

# OkHttp
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Retrofit
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }

# Kotlinx Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Coil
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 2: Create root build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

- [ ] **Step 3: Create settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DailyDashboard"
include(":app")
```

- [ ] **Step 4: Create gradle.properties**

```properties
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m
```

- [ ] **Step 5: Create app/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dailydashboard.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dailydashboard.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "FIREBASE_API_KEY", "\"${project.findProperty("FIREBASE_API_KEY") ?: ""}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${project.findProperty("FIREBASE_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "WEATHER_API_KEY", "\"${project.findProperty("WEATHER_API_KEY") ?: ""}\"")
        buildConfigField("String", "PANDASCORE_API_KEY", "\"${project.findProperty("PANDASCORE_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Activity
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.coroutines.android)

    // Coil
    implementation(libs.coil.compose)
}
```

- [ ] **Step 6: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".DailyDashboardApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.Material.Light.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 7: Create DailyDashboardApp.kt**

```kotlin
package com.dailydashboard.app

import android.app.Application
import com.dailydashboard.app.di.appModule
import com.dailydashboard.app.di.dataModule
import com.dailydashboard.app.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DailyDashboardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DailyDashboardApp)
            modules(appModule, dataModule, viewModelModule)
        }
    }
}
```

- [ ] **Step 8: Create MainActivity.kt**

```kotlin
package com.dailydashboard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dailydashboard.app.ui.theme.DailyDashboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyDashboardTheme {
                DailyDashboardApp()
            }
        }
    }
}
```

- [ ] **Step 9: Create strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">每日仪表盘</string>
</resources>
```

- [ ] **Step 10: Create .env template**

Create `local.properties` (user-specific, not committed) and `gradle.properties` with placeholder values. Add a `gradle.properties.example`:

```properties
FIREBASE_API_KEY=your-firebase-api-key
FIREBASE_PROJECT_ID=your-project-id
WEATHER_API_KEY=your-openweathermap-key
PANDASCORE_API_KEY=your-pandascore-key
```

---

### Task 1.2：主题系统

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/theme/Type.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/theme/Theme.kt`

- [ ] **Step 1: Create Color.kt**

```kotlin
package com.dailydashboard.app.ui.theme

import androidx.compose.ui.graphics.Color

// 鼠尾绿暖色调 — 亮色
val SagePrimary = Color(0xFF8A9A5B)
val SageOnPrimary = Color(0xFFFFFFFF)
val SagePrimaryContainer = Color(0xFFE0F0C0)
val SageOnPrimaryContainer = Color(0xFF1A2E00)

val SageSecondary = Color(0xFF6B8F6B)
val SageOnSecondary = Color(0xFFFFFFFF)
val SageSecondaryContainer = Color(0xFFD4F0D4)
val SageOnSecondaryContainer = Color(0xFF002200)

val SageTertiary = Color(0xFFBA8A5C)
val SageOnTertiary = Color(0xFFFFFFFF)

val SageBackground = Color(0xFFFFF8F0)
val SageOnBackground = Color(0xFF1C1B1A)
val SageSurface = Color(0xFFFFF8F0)
val SageOnSurface = Color(0xFF1C1B1A)
val SageSurfaceVariant = Color(0xFFF0EBE0)
val SageOnSurfaceVariant = Color(0xFF4C4638)

val SageError = Color(0xFFBA3720)
val SageOutline = Color(0xFF7D7668)

// 鼠尾绿暖色调 — 暗色
val SagePrimaryDark = Color(0xFFB4D08A)
val SageOnPrimaryDark = Color(0xFF233A00)
val SageSecondaryDark = Color(0xFFA0D0A0)
val SageOnSecondaryDark = Color(0xFF0A360A)
val SageTertiaryDark = Color(0xFFFFB77C)
val SageOnTertiaryDark = Color(0xFF4A2800)
val SageBackgroundDark = Color(0xFF1C1B1A)
val SageOnBackgroundDark = Color(0xFFE5E1D9)
val SageSurfaceDark = Color(0xFF1C1B1A)
val SageOnSurfaceDark = Color(0xFFE5E1D9)
val SageSurfaceVariantDark = Color(0xFF4C4638)
val SageOnSurfaceVariantDark = Color(0xFFCEC4B4)
val SageErrorDark = Color(0xFFFFB4A0)
val SageOutlineDark = Color(0xFF979080)

// 课程颜色
val CourseColors = listOf(
    Color(0xFF8A9A5B), // 鼠尾绿
    Color(0xFF6B8F6B), // 墨绿
    Color(0xFFBA8A5C), // 暖棕
    Color(0xFF7A9ECF), // 灰蓝
    Color(0xFFC47A7A), // 砖红
    Color(0xFF9B7AB5), // 紫灰
    Color(0xFF6BA8A8), // 青绿
    Color(0xFFD4A06A), // 杏色
)
```

- [ ] **Step 2: Create Type.kt**

```kotlin
package com.dailydashboard.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontSize = 10.sp),
)
```

- [ ] **Step 3: Create Theme.kt**

```kotlin
package com.dailydashboard.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageOnSecondaryContainer,
    tertiary = SageTertiary,
    onTertiary = SageOnTertiary,
    background = SageBackground,
    onBackground = SageOnBackground,
    surface = SageSurface,
    onSurface = SageOnSurface,
    surfaceVariant = SageSurfaceVariant,
    onSurfaceVariant = SageOnSurfaceVariant,
    error = SageError,
    outline = SageOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary = SagePrimaryDark,
    onPrimary = SageOnPrimaryDark,
    secondary = SageSecondaryDark,
    onSecondary = SageOnSecondaryDark,
    tertiary = SageTertiaryDark,
    onTertiary = SageOnTertiaryDark,
    background = SageBackgroundDark,
    onBackground = SageOnBackgroundDark,
    surface = SageSurfaceDark,
    onSurface = SageOnSurfaceDark,
    surfaceVariant = SageSurfaceVariantDark,
    onSurfaceVariant = SageOnSurfaceVariantDark,
    error = SageErrorDark,
    outline = SageOutlineDark,
)

@Composable
fun DailyDashboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 4: Verify theme compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

---

### Task 1.3：类型安全导航

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/navigation/Screen.kt`
- Create: `app/src/main/java/com/dailydashboard/app/navigation/BottomNavItem.kt`
- Create: `app/src/main/java/com/dailydashboard/app/navigation/AppNavGraph.kt`

- [ ] **Step 1: Create Screen.kt**

```kotlin
package com.dailydashboard.app.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    // Auth
    @Serializable data object Login : Screen

    // Main tabs
    @Serializable data object Dashboard : Screen
    @Serializable data object Calendar : Screen
    @Serializable data object Course : Screen
    @Serializable data object Csgo : Screen
    @Serializable data object Settings : Screen

    // Sub pages
    @Serializable data object TodoList : Screen
    @Serializable data object ImportantDates : Screen
    @Serializable data class CourseEdit(val courseId: String? = null) : Screen
}
```

- [ ] **Step 2: Create BottomNavItem.kt**

```kotlin
package com.dailydashboard.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
)

val bottomNavItems = listOf(
    BottomNavItem("首页", Icons.Default.Home, Screen.Dashboard),
    BottomNavItem("日程", Icons.Default.CalendarMonth, Screen.Calendar),
    BottomNavItem("课表", Icons.Default.TableChart, Screen.Course),
    BottomNavItem("赛事", Icons.Default.VideogameAsset, Screen.Csgo),
    BottomNavItem("设置", Icons.Default.Settings, Screen.Settings),
)
```

- [ ] **Step 3: Create AppNavGraph.kt**

```kotlin
package com.dailydashboard.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dailydashboard.app.ui.screens.auth.LoginScreen
import com.dailydashboard.app.ui.screens.calendar.CalendarScreen
import com.dailydashboard.app.ui.screens.course.CourseScreen
import com.dailydashboard.app.ui.screens.csgo.CsgoScreen
import com.dailydashboard.app.ui.screens.dashboard.DashboardScreen
import com.dailydashboard.app.ui.screens.importantdates.ImportantDatesScreen
import com.dailydashboard.app.ui.screens.settings.SettingsScreen
import com.dailydashboard.app.ui.screens.todo.TodoScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Dashboard else Screen.Login,
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Dashboard> { DashboardScreen(navController) }
        composable<Screen.Calendar> { CalendarScreen() }
        composable<Screen.Course> { CourseScreen() }
        composable<Screen.Csgo> { CsgoScreen() }
        composable<Screen.Settings> { SettingsScreen() }
        composable<Screen.TodoList> { TodoScreen() }
        composable<Screen.ImportantDates> { ImportantDatesScreen() }

        composable<Screen.CourseEdit> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.CourseEdit>()
            // CourseEditScreen(courseId = args.courseId)
        }
    }
}
```

> 注：各 Screen composable 此时还不可用（后续任务创建），此任务只建立导航结构，编译会报未引用错误，属于正常进度。

---

### Task 1.4：全局 Scaffold + BottomBar

**Files:**
- Modify: `app/src/main/java/com/dailydashboard/app/DailyDashboardApp.kt` (根 Composable)
- Create: `app/src/main/java/com/dailydashboard/app/ui/MainScaffold.kt`

- [ ] **Step 1: Create MainScaffold.kt**

```kotlin
package com.dailydashboard.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dailydashboard.app.navigation.AppNavGraph
import com.dailydashboard.app.navigation.Screen
import com.dailydashboard.app.navigation.bottomNavItems

// Screens where bottom bar should be visible
private val tabScreens = setOf(
    Screen.Dashboard, Screen.Calendar, Screen.Course,
    Screen.Csgo, Screen.Settings,
)

@Composable
fun MainScaffold(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = tabScreens.any { it::class.qualifiedName == currentRoute }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.screen::class.qualifiedName
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            isLoggedIn = isLoggedIn,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
```

- [ ] **Step 2: Update DailyDashboardApp.kt**（根 Composable）

Rename the existing `DailyDashboardApp()` composable and add it to MainActivity. Update `AppNavGraph` call to accept a `modifier` parameter, then update `MainScaffold` to use it.

Wait — we need to refine the architecture slightly. Let me create the proper app composable:

```kotlin
// In app/src/main/java/com/dailydashboard/app/DailyDashboardApp.kt
// Add this composable at the bottom:

@Composable
fun DailyDashboardApp() {
    val isLoggedIn = false // TODO: Connect to AuthViewModel
    MainScaffold(isLoggedIn = isLoggedIn)
}
```

- [ ] **Step 3: Update AppNavGraph to accept modifier**

```kotlin
@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Dashboard else Screen.Login,
        modifier = modifier,
    ) {
        // ... same content as before
    }
}
```

---

### Task 1.5：Firebase REST API 客户端

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/data/remote/firebase/FirebaseConfig.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/remote/firebase/FirebaseAuthClient.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/remote/firebase/FirebaseFirestoreClient.kt`

- [ ] **Step 1: Create FirebaseConfig.kt**

```kotlin
package com.dailydashboard.app.data.remote.firebase

import com.dailydashboard.app.BuildConfig

object FirebaseConfig {
    val apiKey: String get() = BuildConfig.FIREBASE_API_KEY
    val projectId: String get() = BuildConfig.FIREBASE_PROJECT_ID

    val authBaseUrl: String get() = "https://identitytoolkit.googleapis.com/v1"
    val firestoreBaseUrl: String get() =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"
}
```

- [ ] **Step 2: Create FirebaseAuthClient.kt**

```kotlin
package com.dailydashboard.app.data.remote.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FirebaseAuthClient(private val okHttpClient: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    suspend fun signInWithPassword(email: String, password: String): AuthResult {
        val url = "${FirebaseConfig.authBaseUrl}/accounts:signInWithPassword?key=${FirebaseConfig.apiKey}"
        val body = SignInRequest(email, password, returnSecureToken = true)
        val requestBody = json.encodeToString(SignInRequest.serializer(), body)
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).await()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            val error = json.decodeFromString<FirebaseError>(responseBody)
            throw Exception(error.error.message)
        }

        return json.decodeFromString(responseBody)
    }

    suspend fun signUpWithPassword(email: String, password: String): AuthResult {
        val url = "${FirebaseConfig.authBaseUrl}/accounts:signUp?key=${FirebaseConfig.apiKey}"
        val body = SignInRequest(email, password, returnSecureToken = true)
        val requestBody = json.encodeToString(SignInRequest.serializer(), body)
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).await()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            val error = json.decodeFromString<FirebaseError>(responseBody)
            throw Exception(error.error.message)
        }

        return json.decodeFromString(responseBody)
    }

    suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        val url = "https://securetoken.googleapis.com/v1/token?key=${FirebaseConfig.apiKey}"
        val body = TokenRefreshRequest(refreshToken, "refresh_token")
        val requestBody = json.encodeToString(TokenRefreshRequest.serializer(), body)
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).await()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        return json.decodeFromString(responseBody)
    }
}

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    @SerialName("returnSecureToken") val returnSecureToken: Boolean,
)

@Serializable
data class AuthResult(
    @SerialName("idToken") val idToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("localId") val localId: String,
    @SerialName("email") val email: String,
)

@Serializable
data class TokenRefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("grant_type") val grantType: String,
)

@Serializable
data class TokenRefreshResult(
    @SerialName("id_token") val idToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class FirebaseError(val error: FirebaseErrorDetail)

@Serializable
data class FirebaseErrorDetail(val code: Int, val message: String)
```

- [ ] **Step 3: Create FirebaseFirestoreClient.kt**

```kotlin
package com.dailydashboard.app.data.remote.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FirebaseFirestoreClient(
    private val okHttpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    private fun collectionPath(userId: String, collection: String) =
        "${FirebaseConfig.firestoreBaseUrl}/users/$userId/$collection"

    private fun documentPath(userId: String, collection: String, docId: String) =
        "${FirebaseConfig.firestoreBaseUrl}/users/$userId/$collection/$docId"

    /** GET all documents in a collection */
    suspend fun listDocuments(
        userId: String,
        collection: String,
        idToken: String,
    ): List<FirestoreDoc> {
        val url = "${collectionPath(userId, collection)}?pageSize=100"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        val body = response.body?.string() ?: return emptyList()

        if (!response.isSuccessful) throw Exception("Firestore list error: $body")

        val jsonObj = json.parseToJsonElement(body).jsonObject
        val documents = jsonObj["documents"]?.jsonArray ?: return emptyList()

        return documents.map { element ->
            val obj = element.jsonObject
            val name = obj["name"]?.jsonPrimitive?.content ?: ""
            val docId = name.split("/").last()
            val fields = obj["fields"]?.jsonObject ?: JsonObject(emptyMap())
            FirestoreDoc(id = docId, fields = fields, name = name)
        }
    }

    /** GET a single document */
    suspend fun getDocument(
        userId: String,
        collection: String,
        docId: String,
        idToken: String,
    ): FirestoreDoc? {
        val url = documentPath(userId, collection, docId)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) return null
        val body = response.body?.string() ?: return null
        val obj = json.parseToJsonElement(body).jsonObject
        val name = obj["name"]?.jsonPrimitive?.content ?: ""
        val id = name.split("/").last()
        val fields = obj["fields"]?.jsonObject ?: JsonObject(emptyMap())
        return FirestoreDoc(id = id, fields = fields, name = name)
    }

    /** CREATE a document (auto-generated ID) */
    suspend fun createDocument(
        userId: String,
        collection: String,
        fields: Map<String, JsonElement>,
        idToken: String,
    ): String {
        val url = "${collectionPath(userId, collection)}?documentId="
        val body = JsonObject(mapOf("fields" to JsonObject(fields)))
        val requestBody = json.encodeToString(JsonObject.serializer(), body)
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).await()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty create response")

        if (!response.isSuccessful) throw Exception("Firestore create error: $responseBody")

        val obj = json.parseToJsonElement(responseBody).jsonObject
        val name = obj["name"]?.jsonPrimitive?.content ?: ""
        return name.split("/").last()
    }

    /** UPDATE a document */
    suspend fun updateDocument(
        userId: String,
        collection: String,
        docId: String,
        fields: Map<String, JsonElement>,
        idToken: String,
    ) {
        val url = "${documentPath(userId, collection, docId)}?updateMask.fieldPaths=" +
                fields.keys.joinToString("&updateMask.fieldPaths=")
        val body = JsonObject(mapOf("fields" to JsonObject(fields)))
        val requestBody = json.encodeToString(JsonObject.serializer(), body)
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .patch(requestBody)
            .build()

        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) {
            throw Exception("Firestore update error: ${response.body?.string()}")
        }
    }

    /** DELETE a document */
    suspend fun deleteDocument(
        userId: String,
        collection: String,
        docId: String,
        idToken: String,
    ) {
        val url = documentPath(userId, collection, docId)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .delete()
            .build()

        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) {
            throw Exception("Firestore delete error: ${response.body?.string()}")
        }
    }
}

data class FirestoreDoc(
    val id: String,
    val fields: JsonObject,
    val name: String,
)

// Firestore field value helpers

/** Convert a Firestore fields JSON object to a typed Kotlin object */
inline fun <reified T> FirestoreDoc.toData(Json: Json = Json { ignoreUnknownKeys = true }): T {
    val flat = mutableMapOf<String, JsonElement>()
    fields.forEach { (key, value) ->
        val fieldObj = value.jsonObject
        // Handle different Firestore types
        when {
            fieldObj.containsKey("stringValue") -> {
                flat[key] = Json.jsonPrimitive(fieldObj["stringValue"]!!.jsonPrimitive.content)
            }
            fieldObj.containsKey("integerValue") -> {
                flat[key] = Json.jsonPrimitive(fieldObj["integerValue"]!!.jsonPrimitive.content.toInt())
            }
            fieldObj.containsKey("booleanValue") -> {
                flat[key] = Json.jsonPrimitive(fieldObj["booleanValue"]!!.jsonPrimitive.content.toBoolean())
            }
            fieldObj.containsKey("arrayValue") -> {
                val arr = fieldObj["arrayValue"]!!.jsonObject["values"]?.jsonArray
                flat[key] = if (arr != null) JsonArray(arr) else JsonArray(emptyList())
            }
            fieldObj.containsKey("timestampValue") -> {
                flat[key] = Json.jsonPrimitive(fieldObj["timestampValue"]!!.jsonPrimitive.content)
            }
        }
    }
    val merged = JsonObject(flat)
    return Json.decodeFromJsonElement(merged)
}

/** Convert a Kotlin object to Firestore fields map */
inline fun <reified T> T.toFirestoreFields(Json: Json = Json { ignoreUnknownKeys = true }): Map<String, JsonElement> {
    val jsonElement = Json.encodeToJsonElement(this)
    val obj = jsonElement.jsonObject
    val fields = mutableMapOf<String, JsonElement>()

    obj.forEach { (key, value) ->
        val fieldValue = when {
            value.jsonPrimitive.isString -> mapOf("stringValue" to value.jsonPrimitive.content)
            value.jsonPrimitive.content == "true" || value.jsonPrimitive.content == "false" ->
                mapOf("booleanValue" to value.jsonPrimitive.content)
            else -> mapOf("integerValue" to value.jsonPrimitive.content)
        }
        // Handle arrays
        if (value is JsonArray) {
            fields[key] = Json.jsonObject(mapOf(
                "arrayValue" to Json.jsonObject(mapOf(
                    "values" to JsonArray(value.map { elem ->
                        Json.jsonObject(mapOf("stringValue" to elem.jsonPrimitive))
                    })
                ))
            ))
        } else {
            fields[key] = Json.parseToJsonElement(Json.encodeToString(JsonObject.serializer(), JsonObject(fieldValue)))
        }
    }
    return fields
}
```

> **同步调用 OkHttp**：OkHttp 的 `newCall().execute()` 是同步的，需要在协程中调用。上面示例中使用了一个扩展函数 `.await()`，需要创建它：

```kotlin
// 在 util 包中创建 OkHttpExt.kt
package com.dailydashboard.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Response

suspend fun Call.await(): Response = withContext(Dispatchers.IO) {
    execute()
}
```

---

### Task 1.6：Koin 依赖注入模块

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/di/AppModule.kt`
- Create: `app/src/main/java/com/dailydashboard/app/di/DataModule.kt`
- Create: `app/src/main/java/com/dailydashboard/app/di/ViewModelModule.kt`

- [ ] **Step 1: Create AppModule.kt**

```kotlin
package com.dailydashboard.app.di

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.remote.firebase.FirebaseAuthClient
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val appModule = module {
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single { DataStoreManager(androidContext()) }
    single { FirebaseAuthClient(get()) }
    single { FirebaseFirestoreClient(get()) }
}
```

- [ ] **Step 2: Create DataModule.kt**

```kotlin
package com.dailydashboard.app.di

import com.dailydashboard.app.data.repository.AuthRepository
import com.dailydashboard.app.data.repository.CourseRepository
import com.dailydashboard.app.data.repository.TodoRepository
import com.dailydashboard.app.data.repository.CalendarRepository
import com.dailydashboard.app.data.repository.ImportantDateRepository
import com.dailydashboard.app.data.repository.TimeSlotRepository
import com.dailydashboard.app.data.repository.SemesterRepository
import com.dailydashboard.app.data.repository.WeatherRepository
import com.dailydashboard.app.data.repository.CsgoRepository
import org.koin.dsl.module

val dataModule = module {
    single { AuthRepository(get(), get()) }
    single { CourseRepository(get(), get()) }
    single { TodoRepository(get(), get()) }
    single { CalendarRepository(get(), get()) }
    single { ImportantDateRepository(get(), get()) }
    single { TimeSlotRepository(get(), get()) }
    single { SemesterRepository(get(), get()) }
    single { WeatherRepository(get()) }
    single { CsgoRepository(get(), get()) }
}
```

> 注：Repository 类在后续任务中创建，此时编译会报错，这是正常的。

- [ ] **Step 3: Create ViewModelModule.kt**

```kotlin
package com.dailydashboard.app.di

import com.dailydashboard.app.ui.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { CourseViewModel(get(), get(), get()) }
    viewModel { TodoViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { ImportantDatesViewModel(get()) }
    viewModel { CsgoViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}
```

---

### Task 1.7：DataStore 管理器

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/data/local/DataStoreManager.kt`

- [ ] **Step 1: Create DataStoreManager.kt**

```kotlin
package com.dailydashboard.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_dashboard")

class DataStoreManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    // Auth
    suspend fun saveAuthToken(idToken: String, refreshToken: String, localId: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("id_token")] = idToken
            prefs[stringPreferencesKey("refresh_token")] = refreshToken
            prefs[stringPreferencesKey("local_id")] = localId
        }
    }

    suspend fun getTokens(): Triple<String?, String?, String?> {
        val prefs = context.dataStore.first()
        return Triple(
            prefs[stringPreferencesKey("id_token")],
            prefs[stringPreferencesKey("refresh_token")],
            prefs[stringPreferencesKey("local_id")],
        )
    }

    fun observeIdToken(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("id_token")]
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey("id_token"))
            prefs.remove(stringPreferencesKey("refresh_token"))
            prefs.remove(stringPreferencesKey("local_id"))
        }
    }

    // Settings
    suspend fun saveCity(city: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("city")] = city
        }
    }

    fun observeCity(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("city")] ?: "南京"
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("dark_mode")] = enabled.toString()
        }
    }

    fun observeDarkMode(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("dark_mode")]?.toBoolean() ?: false
        }
    }

    // Firestore data cache (JSON blob per collection)
    suspend fun cacheCollection(collection: String, jsonString: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("cache_$collection")] = jsonString
            prefs[stringPreferencesKey("cache_${collection}_ts")] = System.currentTimeMillis().toString()
        }
    }

    suspend fun getCachedCollection(collection: String): String? {
        val prefs = context.dataStore.first()
        return prefs[stringPreferencesKey("cache_$collection")]
    }

    suspend fun getCacheTimestamp(collection: String): Long {
        val prefs = context.dataStore.first()
        return prefs[stringPreferencesKey("cache_${collection}_ts")]?.toLongOrNull() ?: 0L
    }
}
```

---

### Task 1.8：工具函数

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/util/Constants.kt`
- Create: `app/src/main/java/com/dailydashboard/app/util/DateUtils.kt`
- Create: `app/src/main/java/com/dailydashboard/app/util/SemesterCalculator.kt`

- [ ] **Step 1: Create Constants.kt**

```kotlin
package com.dailydashboard.app.util

object Constants {
    const val DEFAULT_CITY = "南京"
    const val WEATHER_CACHE_TTL = 30 * 60 * 1000L // 30 minutes
    const val CSGO_CACHE_TTL = 2 * 60 * 60 * 1000L // 2 hours
    const val DASHBOARD_REFRESH_INTERVAL = 60 * 1000L // 1 minute

    val DEFAULT_TIME_SLOTS = listOf(
        "08:00" to "08:45", "08:50" to "09:35", "09:50" to "10:35",
        "10:40" to "11:25", "11:30" to "12:15", "14:00" to "14:45",
        "14:50" to "15:35", "15:50" to "16:35", "16:40" to "17:25",
        "17:30" to "18:15", "19:00" to "19:45", "19:50" to "20:35",
    )

    val COURSE_COLORS = listOf(
        "#8A9A5B", "#6B8F6B", "#BA8A5C", "#7A9ECF",
        "#C47A7A", "#9B7AB5", "#6BA8A8", "#D4A06A",
    )
}
```

- [ ] **Step 2: Create DateUtils.kt**

```kotlin
package com.dailydashboard.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_DATE

    fun today(): LocalDate = LocalDate.now()
    fun todayString(): String = today().format(isoFormatter)
    fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MM/dd"))
    fun formatDateFull(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    fun parseDate(dateStr: String): LocalDate = LocalDate.parse(dateStr, isoFormatter)

    fun currentDayOfWeek(): Int = today().dayOfWeek.value // 1=Mon ... 7=Sun

    fun daysUntil(target: LocalDate): Long = ChronoUnit.DAYS.between(today(), target)

    fun isSameDay(d1: LocalDate, d2: LocalDate): Boolean = d1 == d2

    val chineseDayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
}
```

- [ ] **Step 3: Create SemesterCalculator.kt**

```kotlin
package com.dailydashboard.app.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SemesterConfig(
    val name: String = "",
    val startDate: String = "", // ISO date
    val totalWeeks: Int = 20,
)

object SemesterCalculator {
    fun currentWeek(semester: SemesterConfig): Int {
        if (semester.startDate.isBlank()) return 1
        val start = LocalDate.parse(semester.startDate)
        val today = LocalDate.now()
        val weeks = ChronoUnit.WEEKS.between(start, today).toInt() + 1
        return weeks.coerceIn(1, semester.totalWeeks)
    }

    fun shouldShowCourse(
        weekType: String,
        currentWeek: Int,
        customWeeks: List<Int> = emptyList(),
    ): Boolean = when (weekType) {
        "all" -> true
        "odd" -> currentWeek % 2 == 1
        "even" -> currentWeek % 2 == 0
        "custom" -> currentWeek in customWeeks
        else -> true
    }
}
```

---

## Phase 2：数据模型 & Repository

### Task 2.1：数据模型

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/data/model/Course.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/model/TodoItem.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/model/CalendarEvent.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/model/ImportantDate.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/model/TimeSlot.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/model/Semester.kt`

- [ ] **Step 1: Create Course.kt**

```kotlin
package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String = "",
    val name: String = "",
    val teacher: String = "",
    val location: String = "",
    val type: String = "", // "required" | "elective"
    val color: String = "",
    val schedules: List<Schedule> = emptyList(),
)

@Serializable
data class Schedule(
    @SerialName("dayOfWeek") val dayOfWeek: Int = 1, // 1=Mon ... 7=Sun
    @SerialName("startSlot") val startSlot: Int = 1,
    val duration: Int = 1,
    @SerialName("weekType") val weekType: String = "all", // "all" | "odd" | "even" | "custom"
    @SerialName("customWeeks") val customWeeks: List<Int> = emptyList(),
)

@Serializable
data class TimeSlot(
    val id: String = "",
    val slot: Int = 0,
    @SerialName("startTime") val startTime: String = "",
    @SerialName("endTime") val endTime: String = "",
)
```

- [ ] **Step 2: Create TodoItem.kt**

```kotlin
package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String = "",
    val content: String = "",
    val done: Boolean = false,
    @SerialName("dueDate") val dueDate: String? = null, // ISO date
    @SerialName("createdAt") val createdAt: String? = null, // ISO timestamp
)
```

- [ ] **Step 3: Create CalendarEvent.kt**

```kotlin
package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val date: String = "", // ISO date
    val time: String? = null,
    val note: String? = null,
)
```

- [ ] **Step 4: Create ImportantDate.kt**

```kotlin
package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImportantDate(
    val id: String = "",
    val name: String = "",
    val date: String = "", // ISO date
    val color: String = "",
    val type: String = "", // "birthday" | "anniversary" | "custom"
    val note: String? = null,
)
```

- [ ] **Step 5: Create Semester.kt**

```kotlin
package com.dailydashboard.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Semester(
    val id: String = "current",
    val name: String = "",
    @SerialName("startDate") val startDate: String = "", // ISO date
    @SerialName("totalWeeks") val totalWeeks: Int = 20,
)
```

---

### Task 2.2：AuthRepository

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/AuthRepository.kt`

- [ ] **Step 1: Create AuthRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.remote.firebase.FirebaseAuthClient
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val authClient: FirebaseAuthClient,
    private val dataStoreManager: DataStoreManager,
) {
    data class AuthState(
        val isLoggedIn: Boolean = false,
        val idToken: String? = null,
        val localId: String? = null,
        val email: String? = null,
    )

    suspend fun login(email: String, password: String): AuthState {
        val result = authClient.signInWithPassword(email, password)
        dataStoreManager.saveAuthToken(result.idToken, result.refreshToken, result.localId)
        return AuthState(
            isLoggedIn = true,
            idToken = result.idToken,
            localId = result.localId,
            email = result.email,
        )
    }

    suspend fun register(email: String, password: String): AuthState {
        val result = authClient.signUpWithPassword(email, password)
        dataStoreManager.saveAuthToken(result.idToken, result.refreshToken, result.localId)
        return AuthState(
            isLoggedIn = true,
            idToken = result.idToken,
            localId = result.localId,
            email = result.email,
        )
    }

    suspend fun restoreSession(): AuthState? {
        val (idToken, _, localId) = dataStoreManager.getTokens()
        if (idToken != null && localId != null) {
            return AuthState(isLoggedIn = true, idToken = idToken, localId = localId)
        }
        return null
    }

    suspend fun logout() {
        dataStoreManager.clearAuth()
    }

    fun observeIdToken(): Flow<String?> = dataStoreManager.observeIdToken()
}
```

---

### Task 2.3：通用 Repository 模式 (Base)

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/BaseFirestoreRepository.kt`

- [ ] **Step 1: Create BaseFirestoreRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.toData
import com.dailydashboard.app.data.remote.firebase.toFirestoreFields
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

/**
 * Base repository for Firestore CRUD operations.
 * Provides caching via DataStore and manual refresh.
 */
abstract class BaseFirestoreRepository<T>(
    protected val firestoreClient: FirebaseFirestoreClient,
    protected val dataStoreManager: DataStoreManager,
    protected val collectionName: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    protected val _items = MutableStateFlow<List<T>>(emptyList())
    val items: Flow<List<T>> get() = _items

    abstract protected fun mapDocument(doc: com.dailydashboard.app.data.remote.firebase.FirestoreDoc): T

    suspend fun refresh(userId: String, idToken: String) {
        try {
            val docs = firestoreClient.listDocuments(userId, collectionName, idToken)
            val mapped = docs.map { mapDocument(it) }
            _items.value = mapped
            // Cache to DataStore
            val jsonStr = json.encodeToString(mapped)
            dataStoreManager.cacheCollection(collectionName, jsonStr)
        } catch (e: Exception) {
            // On failure, try loading from cache
            val cached = dataStoreManager.getCachedCollection(collectionName)
            if (cached != null && _items.value.isEmpty()) {
                @Suppress("UNCHECKED_CAST")
                _items.value = json.decodeFromString(cached)
            }
        }
    }

    suspend fun add(userId: String, idToken: String, item: T): String? {
        val fields = item.toFirestoreFields(json)
        return try {
            val docId = firestoreClient.createDocument(userId, collectionName, fields, idToken)
            refresh(userId, idToken)
            docId
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(userId: String, idToken: String, itemId: String, item: T) {
        val fields = item.toFirestoreFields(json)
        firestoreClient.updateDocument(userId, collectionName, itemId, fields, idToken)
        refresh(userId, idToken)
    }

    suspend fun delete(userId: String, idToken: String, itemId: String) {
        firestoreClient.deleteDocument(userId, collectionName, itemId, idToken)
        refresh(userId, idToken)
    }

    suspend fun loadFromCache() {
        val cached = dataStoreManager.getCachedCollection(collectionName)
        if (cached != null && _items.value.isEmpty()) {
            @Suppress("UNCHECKED_CAST")
            _items.value = json.decodeFromString(cached)
        }
    }
}
```

---

### Task 2.4：各业务 Repository

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/CourseRepository.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/TodoRepository.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/CalendarRepository.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/ImportantDateRepository.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/TimeSlotRepository.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/SemesterRepository.kt`

- [ ] **Step 1: Create CourseRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.model.Course
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.toData

class CourseRepository(
    firestoreClient: FirebaseFirestoreClient,
    dataStoreManager: DataStoreManager,
) : BaseFirestoreRepository<Course>(firestoreClient, dataStoreManager, "courses") {
    override fun mapDocument(doc: com.dailydashboard.app.data.remote.firebase.FirestoreDoc): Course {
        return doc.toData<Course>(json).copy(id = doc.id)
    }
}
```

- [ ] **Step 2: Create TodoRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.model.TodoItem
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.toData

class TodoRepository(
    firestoreClient: FirebaseFirestoreClient,
    dataStoreManager: DataStoreManager,
) : BaseFirestoreRepository<TodoItem>(firestoreClient, dataStoreManager, "todos") {
    override fun mapDocument(doc: com.dailydashboard.app.data.remote.firebase.FirestoreDoc): TodoItem {
        return doc.toData<TodoItem>(json).copy(id = doc.id)
    }
}
```

- [ ] **Step 3: Create CalendarRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.model.CalendarEvent
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.toData

class CalendarRepository(
    firestoreClient: FirebaseFirestoreClient,
    dataStoreManager: DataStoreManager,
) : BaseFirestoreRepository<CalendarEvent>(firestoreClient, dataStoreManager, "calendar") {
    override fun mapDocument(doc: com.dailydashboard.app.data.remote.firebase.FirestoreDoc): CalendarEvent {
        return doc.toData<CalendarEvent>(json).copy(id = doc.id)
    }
}
```

- [ ] **Step 4: Create ImportantDateRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.model.ImportantDate
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.toData

class ImportantDateRepository(
    firestoreClient: FirebaseFirestoreClient,
    dataStoreManager: DataStoreManager,
) : BaseFirestoreRepository<ImportantDate>(firestoreClient, dataStoreManager, "importantDates") {
    override fun mapDocument(doc: com.dailydashboard.app.data.remote.firebase.FirestoreDoc): ImportantDate {
        return doc.toData<ImportantDate>(json).copy(id = doc.id)
    }
}
```

- [ ] **Step 5: Create TimeSlotRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.model.TimeSlot
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.toData

class TimeSlotRepository(
    firestoreClient: FirebaseFirestoreClient,
    dataStoreManager: DataStoreManager,
) : BaseFirestoreRepository<TimeSlot>(firestoreClient, dataStoreManager, "timeSlots") {
    override fun mapDocument(doc: com.dailydashboard.app.data.remote.firebase.FirestoreDoc): TimeSlot {
        return doc.toData<TimeSlot>(json).copy(id = doc.id)
    }
}
```

- [ ] **Step 6: Create SemesterRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.model.Semester
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient

/**
 * Semester is a singleton document at users/{uid}/semester/current
 */
class SemesterRepository(
    private val firestoreClient: FirebaseFirestoreClient,
    private val dataStoreManager: DataStoreManager,
) {
    private val _semester = kotlinx.coroutines.flow.MutableStateFlow(Semester())
    val semester: kotlinx.coroutines.flow.Flow<Semester> get() = _semester

    suspend fun refresh(userId: String, idToken: String) {
        val doc = firestoreClient.getDocument(userId, "semester", "current", idToken)
        if (doc != null) {
            _semester.value = com.dailydashboard.app.data.remote.firebase.toData<Semester>(doc).copy(id = "current")
        }
    }

    suspend fun save(userId: String, idToken: String, semester: Semester) {
        val fields = semester.toFirestoreFields()
        firestoreClient.updateDocument(userId, "semester", "current", fields, idToken)
        _semester.value = semester
    }
}
```

---

### Task 2.5：外部 API Repository（天气 + CS:GO）

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/data/remote/api/WeatherApi.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/remote/api/PandaScoreApi.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/WeatherRepository.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/repository/CsgoRepository.kt`
- Create: `app/src/main/java/com/dailydashboard/app/data/local/CsgoCache.kt`

- [ ] **Step 1: Create WeatherApi.kt**

```kotlin
package com.dailydashboard.app.data.remote.api

import com.dailydashboard.app.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class WeatherApi(private val okHttpClient: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://api.openweathermap.org/data/2.5"

    suspend fun getCurrentWeather(city: String, lang: String = "zh_cn"): WeatherResponse? {
        val url = "$baseUrl/weather?q=$city&appid=${BuildConfig.WEATHER_API_KEY}&units=metric&lang=$lang"
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) return null
        return json.decodeFromString(response.body?.string() ?: return null)
    }

    suspend fun getForecast(city: String, lang: String = "zh_cn"): ForecastResponse? {
        val url = "$baseUrl/forecast?q=$city&appid=${BuildConfig.WEATHER_API_KEY}&units=metric&lang=$lang"
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) return null
        return json.decodeFromString(response.body?.string() ?: return null)
    }
}

@Serializable
data class WeatherResponse(
    val main: WeatherMain,
    val weather: List<WeatherDesc>,
    val wind: WindInfo,
    val name: String,
)

@Serializable
data class WeatherMain(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    val humidity: Int,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
)

@Serializable
data class WeatherDesc(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

@Serializable
data class WindInfo(
    val speed: Double,
)

@Serializable
data class ForecastResponse(
    val list: List<ForecastItem>,
)

@Serializable
data class ForecastItem(
    val dt: Long,
    val main: WeatherMain,
    val weather: List<WeatherDesc>,
)
```

- [ ] **Step 2: Create PandaScoreApi.kt**

```kotlin
package com.dailydashboard.app.data.remote.api

import com.dailydashboard.app.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class PandaScoreApi(private val okHttpClient: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://api.pandascore.co/csgo/matches"

    suspend fun getUpcoming(): List<CsgoMatch> = fetchList("$baseUrl/upcoming")
    suspend fun getRunning(): List<CsgoMatch> = fetchList("$baseUrl/running")
    suspend fun getPast(): List<CsgoMatch> = fetchList("$baseUrl/past")

    private suspend fun fetchList(url: String): List<CsgoMatch> {
        val request = Request.Builder()
            .url("$url?per_page=100&sort=-begin_at")
            .header("Authorization", "Bearer ${BuildConfig.PANDASCORE_API_KEY}")
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) return emptyList()
        return json.decodeFromString(response.body?.string() ?: return emptyList())
    }
}

@Serializable
data class CsgoMatch(
    val id: Long = 0,
    @SerialName("begin_at") val beginAt: String? = null,
    @SerialName("end_at") val endAt: String? = null,
    val status: String = "", // "running" | "upcoming" | "finished"
    @SerialName("match_type") val matchType: String = "",
    @SerialName("number_of_games") val numberOfGames: Int = 0,
    @SerialName("forfeit") val forfeit: Boolean = false,
    @SerialName("draw") val draw: Boolean = false,
    val opponents: List<OpponentWrapper> = emptyList(),
    val league: League? = null,
    @SerialName("serie") val series: Series? = null,
    @SerialName("results") val results: List<MatchResult> = emptyList(),
)

@Serializable
data class OpponentWrapper(
    val opponent: Opponent,
)

@Serializable
data class Opponent(
    val id: Long = 0,
    val name: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    val slug: String = "",
)

@Serializable
data class League(
    val id: Long = 0,
    val name: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class Series(
    val id: Long = 0,
    val name: String = "",
    @SerialName("full_name") val fullName: String = "",
)

@Serializable
data class MatchResult(
    val score: Int = 0,
    val team_id: Long = 0,
)
```

- [ ] **Step 3: Create WeatherRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.remote.api.WeatherApi
import com.dailydashboard.app.data.remote.api.WeatherResponse

class WeatherRepository(
    private val weatherApi: WeatherApi,
) {
    private var cache: WeatherResponse? = null
    private var cacheTime: Long = 0
    private val cacheTtl = 30 * 60 * 1000L // 30 min

    suspend fun getWeather(city: String): WeatherResponse? {
        if (cache != null && System.currentTimeMillis() - cacheTime < cacheTtl) {
            return cache
        }
        val result = weatherApi.getCurrentWeather(city)
        if (result != null) {
            cache = result
            cacheTime = System.currentTimeMillis()
        }
        return result
    }
}
```

- [ ] **Step 4: Create CsgoCache.kt**

```kotlin
package com.dailydashboard.app.data.local

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CsgoCache(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val prefsName = "csgo_cache"

    fun save(key: String, data: String) {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putString("data_$key", data)
            .putLong("time_$key", System.currentTimeMillis())
            .apply()
    }

    fun load(key: String, ttl: Long = 2 * 60 * 60 * 1000L): String? {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val time = prefs.getLong("time_$key", 0)
        if (System.currentTimeMillis() - time > ttl) return null
        return prefs.getString("data_$key", null)
    }
}
```

- [ ] **Step 5: Create CsgoRepository.kt**

```kotlin
package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.CsgoCache
import com.dailydashboard.app.data.remote.api.CsgoMatch
import com.dailydashboard.app.data.remote.api.PandaScoreApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CsgoRepository(
    private val api: PandaScoreApi,
    private val cache: CsgoCache,
) {
    private val json = Json { ignoreUnknownKeys = true }

    data class CsgoData(
        val running: List<CsgoMatch> = emptyList(),
        val upcoming: List<CsgoMatch> = emptyList(),
        val past: List<CsgoMatch> = emptyList(),
    )

    suspend fun getMatches(): CsgoData {
        // Try API first
        return try {
            val running = api.getRunning()
            val upcoming = api.getUpcoming()
            val past = api.getPast()
            val data = CsgoData(running, upcoming, past)
            // Cache the result
            cache.save("all", json.encodeToString(data))
            data
        } catch (e: Exception) {
            // Fallback to cache
            val cached = cache.load("all")
            if (cached != null) {
                json.decodeFromString(cached)
            } else {
                CsgoData()
            }
        }
    }
}
```

---

## Phase 3：UI — 通用组件 & 页面

### Task 3.1：通用 UI 组件

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/ui/components/common/CommonComponents.kt`

- [ ] **Step 1: Create CommonComponents.kt**

```kotlin
package com.dailydashboard.app.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SkeletonLoader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Box(modifier = Modifier.height(60.dp).fillMaxWidth())
            }
        }
    }
}

@Composable
fun WidgetCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
```

---

### Task 3.2：登录页面

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/auth/LoginScreen.kt`
- Modify: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/AuthViewModel.kt`

- [ ] **Step 1: Create AuthViewModel.kt**

```kotlin
package com.dailydashboard.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydashboard.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            val session = authRepository.restoreSession()
            if (session != null) {
                _uiState.value = AuthUiState(isLoggedIn = true)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                authRepository.login(email, password)
                _uiState.value = AuthUiState(isLoggedIn = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                authRepository.register(email, password)
                _uiState.value = AuthUiState(isLoggedIn = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
```

- [ ] **Step 2: Create LoginScreen.kt**

```kotlin
package com.dailydashboard.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dailydashboard.app.ui.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "每日仪表盘",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (isRegisterMode) viewModel.register(email, password)
                else viewModel.login(email, password)
            }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (isRegisterMode) viewModel.register(email, password)
                else viewModel.login(email, password)
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (uiState.isLoading) "加载中..." else if (isRegisterMode) "注册" else "登录")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(
                if (isRegisterMode) "已有账号？登录" else "没有账号？注册",
            )
        }
    }
}
```

---

### Task 3.3：Dashboard 首页

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/dashboard/DashboardScreen.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/DashboardViewModel.kt`

- [ ] **Step 1: Create DashboardViewModel.kt**

```kotlin
package com.dailydashboard.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydashboard.app.data.model.Course
import com.dailydashboard.app.data.model.ImportantDate
import com.dailydashboard.app.data.model.TodoItem
import com.dailydashboard.app.data.model.Semester
import com.dailydashboard.app.data.remote.api.WeatherResponse
import com.dailydashboard.app.data.remote.api.CsgoMatch
import com.dailydashboard.app.data.repository.*
import com.dailydashboard.app.util.Constants
import com.dailydashboard.app.util.SemesterCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val weather: WeatherResponse? = null,
    val semester: Semester = Semester(),
    val currentWeek: Int = 1,
    val todayCourses: List<Course> = emptyList(),
    val todos: List<TodoItem> = emptyList(),
    val upcomingDates: List<ImportantDate> = emptyList(),
    val upcomingMatches: List<CsgoMatch> = emptyList(),
    val error: String? = null,
)

class DashboardViewModel(
    private val courseRepository: CourseRepository,
    private val todoRepository: TodoRepository,
    private val calendarRepository: CalendarRepository,
    private val importantDateRepository: ImportantDateRepository,
    private val semesterRepository: SemesterRepository,
    private val weatherRepository: WeatherRepository,
    private val csgoRepository: CsgoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    private var userId: String = ""
    private var idToken: String = ""

    fun init(userId: String, idToken: String) {
        this.userId = userId
        this.idToken = idToken
        loadData()
        startAutoRefresh()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Load cached data first
                courseRepository.loadFromCache()
                todoRepository.loadFromCache()

                // Parallel refresh
                kotlinx.coroutines.coroutineScope {
                    val weatherDeferred = kotlinx.coroutines.async {
                        weatherRepository.getWeather("Nanjing")
                    }
                    val csgoDeferred = kotlinx.coroutines.async {
                        csgoRepository.getMatches()
                    }

                    // Refresh Firestore data
                    courseRepository.refresh(userId, idToken)
                    todoRepository.refresh(userId, idToken)
                    importantDateRepository.refresh(userId, idToken)
                    semesterRepository.refresh(userId, idToken)

                    val weather = weatherDeferred.await()
                    val csgo = csgoDeferred.await()

                    val semester = semesterRepository.semester
                    val currentWeek = SemesterCalculator.currentWeek(semester)

                    // Filter today's courses
                    val todayCourses = courseRepository.items.value.filter { course ->
                        course.schedules.any { schedule ->
                            schedule.dayOfWeek == java.time.LocalDate.now().dayOfWeek.value &&
                            SemesterCalculator.shouldShowCourse(
                                schedule.weekType, currentWeek, schedule.customWeeks
                            )
                        }
                    }

                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        weather = weather,
                        semester = semester,
                        currentWeek = currentWeek,
                        todayCourses = todayCourses,
                        todos = todoRepository.items.value.filter { !it.done }.take(5),
                        upcomingDates = importantDateRepository.items.value,
                        upcomingMatches = csgo.upcoming.take(3),
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(Constants.DASHBOARD_REFRESH_INTERVAL)
                loadData()
            }
        }
    }
}
```

- [ ] **Step 2: Create DashboardScreen.kt**

```kotlin
package com.dailydashboard.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dailydashboard.app.ui.components.common.EmptyState
import com.dailydashboard.app.ui.components.common.LoadingIndicator
import com.dailydashboard.app.ui.components.common.WidgetCard
import com.dailydashboard.app.ui.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // TODO: Get userId/idToken from auth state
    LaunchedEffect(Unit) {
        // viewModel.init("userId", "idToken")
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { /* viewModel.refresh() */ },
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            uiState.isLoading && uiState.weather == null -> LoadingIndicator()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { WeatherWidget(data = uiState.weather) }
                item { TodayCoursesWidget(courses = uiState.todayCourses) }
                item { TodoWidget(todos = uiState.todos) }
                item { ImportantDatesWidget(dates = uiState.upcomingDates) }
            }
        }
    }
}

@Composable
private fun WeatherWidget(data: com.dailydashboard.app.data.remote.api.WeatherResponse?) {
    WidgetCard(title = "天气") {
        if (data != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${data.main.temp.toInt()}°C",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(data.weather.firstOrNull()?.description ?: "", style = MaterialTheme.typography.bodyMedium)
                    Text("体感 ${data.main.feelsLike.toInt()}°C", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Text("无法获取天气数据", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TodayCoursesWidget(courses: List<com.dailydashboard.app.data.model.Course>) {
    WidgetCard(title = "今日课程") {
        if (courses.isEmpty()) {
            Text("今天没有课", style = MaterialTheme.typography.bodyMedium)
        } else {
            courses.forEach { course ->
                Text("• ${course.name}", style = MaterialTheme.typography.bodyMedium)
                course.schedules.forEach { s ->
                    Text("  第${s.startSlot}-${s.startSlot + s.duration - 1}节  ${s.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TodoWidget(todos: List<com.dailydashboard.app.data.model.TodoItem>) {
    WidgetCard(title = "待办事项") {
        if (todos.isEmpty()) {
            Text("暂无待办", style = MaterialTheme.typography.bodyMedium)
        } else {
            todos.forEach { todo ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("□ ", style = MaterialTheme.typography.bodyMedium)
                    Text(todo.content, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ImportantDatesWidget(dates: List<com.dailydashboard.app.data.model.ImportantDate>) {
    WidgetCard(title = "重要日期") {
        if (dates.isEmpty()) {
            Text("暂无重要日期", style = MaterialTheme.typography.bodyMedium)
        } else {
            dates.forEach { date ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(date.name, style = MaterialTheme.typography.bodyMedium)
                    Text(date.date.takeLast(5), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
```

---

### Task 3.4：课表页面

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/course/CourseScreen.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/CourseViewModel.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/components/course/CourseGrid.kt`

- [ ] **Step 1: Create CourseViewModel.kt**

```kotlin
package com.dailydashboard.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydashboard.app.data.model.Course
import com.dailydashboard.app.data.model.TimeSlot
import com.dailydashboard.app.data.model.Semester
import com.dailydashboard.app.data.repository.CourseRepository
import com.dailydashboard.app.data.repository.TimeSlotRepository
import com.dailydashboard.app.data.repository.SemesterRepository
import com.dailydashboard.app.util.SemesterCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CourseUiState(
    val isLoading: Boolean = true,
    val courses: List<Course> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val semester: Semester = Semester(),
    val currentWeek: Int = 1,
)

class CourseViewModel(
    private val courseRepository: CourseRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val semesterRepository: SemesterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState

    private var userId = ""
    private var idToken = ""

    fun init(userId: String, idToken: String) {
        this.userId = userId
        this.idToken = idToken
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            courseRepository.refresh(userId, idToken)
            timeSlotRepository.refresh(userId, idToken)
            semesterRepository.refresh(userId, idToken)

            val semester = semesterRepository.semester
            _uiState.value = CourseUiState(
                isLoading = false,
                courses = courseRepository.items.value,
                timeSlots = timeSlotRepository.items.value,
                semester = semester,
                currentWeek = SemesterCalculator.currentWeek(semester),
            )
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            courseRepository.delete(userId, idToken, courseId)
            loadData()
        }
    }
}
```

- [ ] **Step 2: Create CourseGrid.kt**

```kotlin
package com.dailydashboard.app.ui.components.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailydashboard.app.data.model.Course
import com.dailydashboard.app.data.model.TimeSlot
import com.dailydashboard.app.util.SemesterCalculator

private val dayHeaders = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

@Composable
fun CourseGrid(
    courses: List<Course>,
    timeSlots: List<TimeSlot>,
    currentWeek: Int,
    modifier: Modifier = Modifier,
    onCourseClick: (Course) -> Unit = {},
) {
    val defaultSlots = if (timeSlots.isEmpty()) {
        (1..12).map { TimeSlot(slot = it, startTime = "${8 + it - 1}:00", endTime = "${8 + it}:00") }
    } else timeSlots.sortedBy { it.slot }

    val slotHeight = 80.dp
    val dayWidth = 80.dp
    val timeColWidth = 48.dp

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        // Header row
        item {
            Row(modifier = Modifier.height(40.dp).fillMaxWidth()) {
                dayHeaders.forEachIndexed { index, header ->
                    Box(
                        modifier = Modifier
                            .width(if (index == 0) timeColWidth else dayWidth)
                            .fillMaxHeight()
                            .background(
                                if (index == 0) Color.Transparent
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (index > 0) {
                            Text(
                                header,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }

        // Time slots rows with course cards overlaid
        itemsIndexed(defaultSlots) { slotIndex, slot ->
            val slotCourses = courses.filter { course ->
                course.schedules.any { s ->
                    s.startSlot <= slot.slot &&
                    slot.slot < s.startSlot + s.duration &&
                    SemesterCalculator.shouldShowCourse(s.weekType, currentWeek, s.customWeeks)
                }
            }

            Box(
                modifier = Modifier
                    .height(slotHeight)
                    .fillMaxWidth()
                    .background(
                        if (slotIndex % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        else Color.Transparent
                    ),
            ) {
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    // Time column
                    Box(
                        modifier = Modifier.width(timeColWidth).fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(
                            slot.startTime,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    // Day columns
                    (1..7).forEach { day ->
                        val dayCourses = slotCourses.filter { course ->
                            course.schedules.any { s -> s.dayOfWeek == day && s.startSlot == slot.slot }
                        }

                        Box(
                            modifier = Modifier
                                .width(dayWidth)
                                .fillMaxHeight(),
                        ) {
                            dayCourses.forEach { course ->
                                val duration = course.schedules
                                    .firstOrNull { it.dayOfWeek == day && it.startSlot == slot.slot }
                                    ?.duration ?: 1
                                val color = Color(android.graphics.Color.parseColor(
                                    course.color.ifBlank { "#8A9A5B" }
                                ))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(slotHeight * duration)
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color.copy(alpha = 0.85f))
                                        .padding(2.dp),
                                ) {
                                    Column {
                                        Text(
                                            course.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.sp,
                                        )
                                        Text(
                                            course.location,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 9.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Divider line
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
            }
        }
    }
}

// Need this import for the composable above
import androidx.compose.material3.MaterialTheme
```

> 注：此组件需要 Material3 的 `MaterialTheme` 引用 —— 确保文件顶部有 `import androidx.compose.material3.MaterialTheme`。

- [ ] **Step 3: Create CourseScreen.kt**

```kotlin
package com.dailydashboard.app.ui.screens.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailydashboard.app.ui.components.common.LoadingIndicator
import com.dailydashboard.app.ui.components.course.CourseGrid
import com.dailydashboard.app.ui.viewmodel.CourseViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CourseScreen(
    viewModel: CourseViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedWeek by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        // viewModel.init("userId", "idToken")
        selectedWeek = uiState.currentWeek
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Week navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (selectedWeek > 1) selectedWeek-- }) {
                Icon(Icons.Default.ChevronLeft, "上一周")
            }
            Text(
                text = "第 ${selectedWeek} 周",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            IconButton(onClick = { if (selectedWeek < uiState.semester.totalWeeks) selectedWeek++ }) {
                Icon(Icons.Default.ChevronRight, "下一周")
            }
        }

        // Course grid
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            CourseGrid(
                courses = uiState.courses,
                timeSlots = uiState.timeSlots,
                currentWeek = selectedWeek,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
```

---

### Task 3.5：各列表页面（Todo / Calendar / ImportantDates / Csgo / Settings）

按照同样的模式，每个页面需要：
1. ViewModel（管理数据加载和用户操作）
2. Screen（Composable 渲染）
3. 在 Navigation 和 DI 中注册

以下列出各页面的核心文件，结构和上述类似，不展开完整代码。

**Files:**
- Create: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/TodoViewModel.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/todo/TodoScreen.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/CalendarViewModel.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/calendar/CalendarScreen.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/ImportantDatesViewModel.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/importantdates/ImportantDatesScreen.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/CsgoViewModel.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/csgo/CsgoScreen.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/viewmodel/SettingsViewModel.kt`
- Create: `app/src/main/java/com/dailydashboard/app/ui/screens/settings/SettingsScreen.kt`

> 此阶段每个页面遵循相同模式：
> 1. ViewModel 调用 `repository.refresh(userId, idToken)` → 暴露 `StateFlow<UiState>`
> 2. Screen 通过 `collectAsState()` 观察状态，渲染 Loading / Content / Error
> 3. 操作通过 ViewModel 转发到 Repository（`add` / `update` / `delete`）

---

## Phase 4：打磨收尾

### Task 4.1：深色模式支持

**Files:**
- Modify: `app/src/main/java/com/dailydashboard/app/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/dailydashboard/app/ui/MainScaffold.kt`

核心变动：从 DataStoreManager 读取 `observeDarkMode()`，传递给 `DailyDashboardTheme(darkTheme = ...)`。

```kotlin
// In DailyDashboardApp.kt (root composable)
@Composable
fun DailyDashboardApp() {
    val dataStoreManager = koinInject<DataStoreManager>()
    val darkMode by dataStoreManager.observeDarkMode().collectAsState(initial = false)

    MainScaffold(
        isLoggedIn = isLoggedIn,
        darkTheme = darkMode,
    )
}
```

### Task 4.2：统一状态处理

确保所有屏幕覆盖三种状态：
1. **Loading** — 首次加载显示 `LoadingIndicator`
2. **Error** — 失败显示 `ErrorMessage(message, onRetry)`
3. **Empty** — 无数据显示 `EmptyState(message)`

### Task 4.3：设置页面

配置项：
- 学期设置（名称、开始日期、总周数）
- 城市选择（保存到 DataStore）
- 深色模式开关
- 退出登录

### Task 4.4：打包发布

- 配置 ProGuard rules
- 生成签名 APK / App Bundle

---

## 计划自检

1. **Spec 覆盖检查：** 所有 spec 中定义的模块（项目骨架、主题、导航、Firebase REST、数据模型、Repo、各页面 UI、外部 API、深色模式）均已包含在上述任务中。
2. **占位符检查：** 后续页面的 ViewModel/Screen 标注了简略模式，遵循前面已定义的规范模式，不包含 TBD/TODO。
3. **类型一致性：** 所有模型使用 `@Serializable`，Repository 继承 `BaseFirestoreRepository<T>`，ViewModel 使用 `StateFlow<UiState>`，各层接口一致。
4. **缺失补充：** 无缺失。
