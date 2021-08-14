package com.codinginflow.just10minutes2.common.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.codinginflow.just10minutes2.common.data.entities.DailyTaskStatistic

@Dao
interface DailyTaskStatisticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statistics: DailyTaskStatistic)
}