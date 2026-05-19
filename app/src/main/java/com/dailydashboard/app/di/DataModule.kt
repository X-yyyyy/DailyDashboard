package com.dailydashboard.app.di

import com.dailydashboard.app.data.repository.AuthRepository
import com.dailydashboard.app.data.repository.CourseRepository
import com.dailydashboard.app.data.repository.TodoRepository
import com.dailydashboard.app.data.repository.CalendarRepository
import com.dailydashboard.app.data.repository.ImportantDateRepository
import com.dailydashboard.app.data.repository.TimeSlotRepository
import com.dailydashboard.app.data.repository.SemesterRepository
import org.koin.dsl.module

val dataModule = module {
    single { AuthRepository(get(), get()) }
    single { CourseRepository(get()) }
    single { TodoRepository(get()) }
    single { CalendarRepository(get()) }
    single { ImportantDateRepository(get()) }
    single { TimeSlotRepository(get()) }
    single { SemesterRepository(get(), get()) }
}
