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