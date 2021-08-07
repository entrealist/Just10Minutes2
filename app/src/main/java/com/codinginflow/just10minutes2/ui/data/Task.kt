package com.codinginflow.just10minutes2.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tasks")
data class Task(
    val name: String,
    val dailyMinutesGoal: Int = 10,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)