package com.codinginflow.just10minutes2.ui.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "daily_task_statistics",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DailyTaskStatistic(
    val taskId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val minutesTarget: Int,
    val minutesCompleted: Int
) {
    val taskCompleted: Boolean
        get() = minutesCompleted >= minutesTarget
}