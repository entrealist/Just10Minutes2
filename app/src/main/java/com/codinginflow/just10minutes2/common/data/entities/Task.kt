package com.codinginflow.just10minutes2.common.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    val name: String,
    val dailyGoalInMinutes: Int = 10,
    val millisCompletedToday: Long = 0,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {
    val dailyGoalInMilliseconds: Long
        get() = dailyGoalInMinutes * 60 * 1000L

    val minutesCompletedToday: Int
        get() = (millisCompletedToday / (60 * 1000)).toInt()

    val millisLeftToday: Long
        get() = dailyGoalInMilliseconds - millisCompletedToday

    val isCompletedToday: Boolean
        get() = millisLeftToday <= 0

    companion object {
        const val NO_ID = -1L
    }
}