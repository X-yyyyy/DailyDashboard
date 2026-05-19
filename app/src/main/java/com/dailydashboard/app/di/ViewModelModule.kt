package com.dailydashboard.app.di

import com.dailydashboard.app.ui.viewmodel.CourseViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { CourseViewModel(get(), get(), get()) }
}
