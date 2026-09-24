package com.example.planner.di

import android.content.Context
import androidx.room.Room
import com.example.planner.data.local.AppDatabase
import com.example.planner.data.local.DayEventDao
import com.example.planner.data.prefs.SettingsManager
import com.example.planner.data.repository.EventRepository
import com.example.planner.notification.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "planner.db").build()

    @Provides
    fun provideDao(db: AppDatabase): DayEventDao = db.dayEventDao()

    @Provides @Singleton
    fun provideRepository(dao: DayEventDao): EventRepository = EventRepository(dao)

    @Provides @Singleton
    fun provideSettings(@ApplicationContext ctx: Context): SettingsManager = SettingsManager(ctx)

    @Provides @Singleton
    fun provideNotificationHelper(@ApplicationContext ctx: Context): NotificationHelper =
        NotificationHelper(ctx)
}