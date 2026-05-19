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
