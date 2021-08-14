package com.codinginflow.just10minutes2.common.di

import android.app.Application
import androidx.room.Room
import com.codinginflow.just10minutes2.common.data.db.JTMDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: JTMDatabase.Callback
    ) = Room.databaseBuilder(app, JTMDatabase::class.java, "jtm_database")
        .addCallback(callback)
        .build()

    @Provides
    fun provideTaskDao(db: JTMDatabase) = db.taskDao()

    @Provides
    fun provideDailyTaskStatisticsDao(db: JTMDatabase) = db.dailyTaskStatisticsDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope