package com.codinginflow.just10minutes2.common.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codinginflow.just10minutes2.common.data.entities.DailyTaskStatistic
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskStatisticsDao {

    @Query("SELECT * FROM daily_task_statistics ORDER BY dayTimestamp DESC")
    fun getAllTaskStatistics(): Flow<List<DailyTaskStatistic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statistics: DailyTaskStatistic)
}