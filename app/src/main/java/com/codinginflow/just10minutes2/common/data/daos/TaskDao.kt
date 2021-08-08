package com.codinginflow.just10minutes2.common.data.daos

import androidx.room.*
import com.codinginflow.just10minutes2.common.data.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("UPDATE tasks SET millisCompletedToday = millisCompletedToday + :amount WHERE id = :taskId")
    suspend fun increaseMillisCompletedToday(taskId: Long, amount: Long)

    @Delete
    suspend fun delete(task: Task)
}