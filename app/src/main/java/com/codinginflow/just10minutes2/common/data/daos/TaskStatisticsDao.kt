package com.codinginflow.just10minutes2.common.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codinginflow.just10minutes2.common.data.entities.TaskStatistic
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskStatisticsDao {

    @Query("SELECT * FROM daily_task_statistics ORDER BY dayTimestamp DESC")
    fun getAllTaskStatistics(): Flow<List<TaskStatistic>>

    @Query("SELECT * FROM daily_task_statistics WHERE taskId = :taskId ORDER BY dayTimestamp DESC")
    fun getTaskStatisticsForTaskId(taskId: Long): Flow<List<TaskStatistic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statistics: TaskStatistic)
}