package com.codinginflow.just10minutes2.common.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "daily_task_statistics",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["taskId", "dayTimestamp"]
)
data class TaskStatistic(
    val taskId: Long,
    val dayTimestamp: Long,
    val timeGoalInMinutes: Int,
    val timeCompletedInMilliseconds: Long,
) {
    val timeGoalInMilliseconds: Long
        get() = timeGoalInMinutes * 60 * 1000L

    val taskCompleted: Boolean
        get() = timeCompletedInMilliseconds >= timeGoalInMinutes * 60 * 1000L
}