package com.codinginflow.just10minutes2.common.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.just10minutes2.common.data.daos.TaskStatisticsDao
import com.codinginflow.just10minutes2.common.data.daos.TaskDao
import com.codinginflow.just10minutes2.common.data.entities.TaskStatistic
import com.codinginflow.just10minutes2.common.data.entities.Task
import com.codinginflow.just10minutes2.common.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class, TaskStatistic::class], version = 1)
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
            val statisticsDao = database.get().dailyTaskStatisticsDao() // TODO: 15.08.2021 Remove this after testing

            applicationScope.launch {
                dao.insert(Task("Read from a book", 1))
                dao.insert(Task("Stretch"))
                dao.insert(Task("Practice guitar", 20))
                repeat(20) {
                    dao.insert(Task("Task #$it"))
                }

                statisticsDao.insert(TaskStatistic(1, 1628892000000, 1, 1 * 60_000L))
                statisticsDao.insert(TaskStatistic(1, 1628805600000, 2, 2 * 60_000L))
                statisticsDao.insert(TaskStatistic(1, 1628719200000, 1, 0 * 60_000L))
                statisticsDao.insert(TaskStatistic(1, 1628632800000, 1, 1 * 60_000L))
                statisticsDao.insert(TaskStatistic(1, 1628546400000, 1, 1 * 60_000L))
                statisticsDao.insert(TaskStatistic(1, 1628460000000, 1, 0 * 60_000L))
                statisticsDao.insert(TaskStatistic(1, 1628373600000, 1, 1 * 60_000L))
                statisticsDao.insert(TaskStatistic(1, 1628287200000, 1, 1 * 60_000L))

                statisticsDao.insert(TaskStatistic(2, 1628892000000, 10, 10 * 60_000L))
                statisticsDao.insert(TaskStatistic(2, 1628805600000, 10, 0 * 60_000L))
                statisticsDao.insert(TaskStatistic(2, 1628719200000, 10, 7 * 60_000L))
                statisticsDao.insert(TaskStatistic(2, 1628632800000, 10, 10 * 60_000L))
                statisticsDao.insert(TaskStatistic(2, 1628546400000, 10, 10 * 60_000L))
                statisticsDao.insert(TaskStatistic(2, 1628460000000, 10, 10 * 60_000L))
                statisticsDao.insert(TaskStatistic(2, 1628373600000, 10, 15 * 60_000L))
                statisticsDao.insert(TaskStatistic(2, 1628287200000, 10, 12 * 60_000L))

                statisticsDao.insert(TaskStatistic(3, 1628892000000, 8, 8 * 60_000L))
                statisticsDao.insert(TaskStatistic(3, 1628805600000, 8, 8 * 60_000L))
                statisticsDao.insert(TaskStatistic(3, 1628719200000, 10, 7 * 60_000L))
                statisticsDao.insert(TaskStatistic(3, 1628632800000, 10, 10 * 60_000L))
                statisticsDao.insert(TaskStatistic(3, 1628546400000, 20, 20 * 60_000L))
                statisticsDao.insert(TaskStatistic(3, 1628460000000, 20, 15 * 60_000L))
                statisticsDao.insert(TaskStatistic(3, 1628373600000, 20, 15 * 60_000L))
                statisticsDao.insert(TaskStatistic(3, 1628287200000, 20, 20 * 60_000L))
            }
        }
    }
}