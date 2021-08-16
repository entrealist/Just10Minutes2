package com.codinginflow.just10minutes2.common.data.entities

import android.content.Context
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.codinginflow.just10minutes2.R
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    val name: String,
    val weekdays: WeekdaySelection = WeekdaySelection(true),
    val dailyGoalInMinutes: Int = 10,
    val timeCompletedTodayInMilliseconds: Long = 0,
    val archived: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) : Parcelable {
    val dailyGoalInMilliseconds: Long
        get() = dailyGoalInMinutes * 60 * 1000L

    val timeCompletedTodayInMinutes: Int
        get() = (timeCompletedTodayInMilliseconds / (60 * 1000)).toInt()

    val timeLeftTodayInMilliseconds: Long
        get() = dailyGoalInMilliseconds - timeCompletedTodayInMilliseconds

    val isCompletedToday: Boolean
        get() = timeLeftTodayInMilliseconds <= 0

    companion object {
        const val NO_ID = -1L
    }
}

@Parcelize
data class WeekdaySelection(
    val mondayActive: Boolean,
    val tuesdayActive: Boolean,
    val wednesdayActive: Boolean,
    val thursdayActive: Boolean,
    val fridayActive: Boolean,
    val saturdayActive: Boolean,
    val sundayActive: Boolean
) : Parcelable {
    constructor(allDays: Boolean) : this(
        allDays,
        allDays,
        allDays,
        allDays,
        allDays,
        allDays,
        allDays
    )
}

fun WeekdaySelection.toLocalizedString(context: Context): String {
    when {
        mondayActive && tuesdayActive && wednesdayActive && thursdayActive && fridayActive && saturdayActive && sundayActive -> {
            return context.getString(R.string.all_weekdays)
        }
        !mondayActive && !tuesdayActive && !wednesdayActive && !thursdayActive && !fridayActive && !saturdayActive && !sundayActive -> {
            return context.getString(R.string.never)
        }
        else -> {
            val weekdaysString = StringBuilder()
            if (mondayActive) {
                weekdaysString.append(context.getString(R.string.monday_abbrev))
                    .append(", ")
            }
            if (tuesdayActive) {
                weekdaysString.append(context.getString(R.string.tuesday_abbrev))
                    .append(", ")
            }
            if (wednesdayActive) {
                weekdaysString.append(context.getString(R.string.wednesday_abbrev))
                    .append(", ")
            }
            if (thursdayActive) {
                weekdaysString.append(context.getString(R.string.thursday_abbrev))
                    .append(", ")
            }
            if (fridayActive) {
                weekdaysString.append(context.getString(R.string.friday_abbrev))
                    .append(", ")
            }
            if (saturdayActive) {
                weekdaysString.append(context.getString(R.string.saturday_abbrev))
                    .append(", ")
            }
            if (sundayActive) {
                weekdaysString.append(context.getString(R.string.sunday_abbrev))
                    .append(", ")
            }
            return weekdaysString.toString().removeSuffix(", ")
        }
    }
}

fun WeekdaySelection.containsWeekdayOfDate(date: Calendar): Boolean =
    date.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && mondayActive ||
    date.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && tuesdayActive ||
    date.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && wednesdayActive ||
    date.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY && thursdayActive ||
    date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && fridayActive ||
    date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && saturdayActive ||
    date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && sundayActive