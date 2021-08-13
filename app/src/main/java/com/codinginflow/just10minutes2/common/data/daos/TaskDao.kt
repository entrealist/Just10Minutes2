package com.codinginflow.just10minutes2.common.data.daos

import androidx.room.*
import com.codinginflow.just10minutes2.common.data.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE archived = 0")
    fun getAllNotArchivedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND archived = 0")
    fun getNotArchivedTaskById(taskId: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("UPDATE tasks SET millisCompletedToday = millisCompletedToday + :amount WHERE id = :taskId")
    suspend fun increaseMillisCompletedToday(taskId: Long, amount: Long)

    @Query("UPDATE tasks SET millisCompletedToday = 0 WHERE id = :taskId")
    suspend fun resetMillisCompletedTodayForTask(taskId: Long)

    @Query("UPDATE tasks SET archived = :archived WHERE id = :taskId")
    suspend fun setArchivedState(taskId: Long, archived: Boolean)

    @Delete
    suspend fun deleteTask(task: Task)
}