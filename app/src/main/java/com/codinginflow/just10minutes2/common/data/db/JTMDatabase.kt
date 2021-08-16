package com.codinginflow.just10minutes2.common.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.just10minutes2.common.data.daos.TaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.TaskStatistic
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.data.entities.WeekdaySelection
import com.codinginflow.just10minutes2.common.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class, TaskStatistic::class], version = 1)
@TypeConverters(Converters::class)
abstract class JTMDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun dailyTaskStatisticsDao(): TaskStatisticsDao

    class Callback @Inject constructor(
        private val database: Provider<JTMDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(
                    Task(
                        name = "Stretch",
                        weekdays = WeekdaySelection(false, true, true, false, true, false, false),
                        dailyGoalInMinutes = 5
                    )
                )
                dao.insert(Task(name = "Read from a book"))
                dao.insert(
                    Task(
                        name = "Practice guitar",
                        weekdays = WeekdaySelection(false, false, true, true, true, true, true),
                        dailyGoalInMinutes = 20
                    )
                )
            }
        }
    }
}