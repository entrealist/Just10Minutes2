package com.codinginflow.just10minutes2.common.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    val name: String,
    val dailyMinutesGoal: Int = 10,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {
    companion object {
        const val ID_NONE = -1L
    }
}