package com.codinginflow.just10minutes2.common.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val minutesCompleted: Int,
    @PrimaryKey(autoGenerate = true) val id: Long = 0

) {
    val taskCompleted: Boolean
        get() = minutesCompleted >= minutesTarget
}