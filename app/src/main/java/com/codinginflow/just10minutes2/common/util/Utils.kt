package com.codinginflow.just10minutes2.common.util

import java.util.*

fun formatTimeText(timeInMillis: Long): String {
    val millisAdjusted = if (timeInMillis < 0) 0 else timeInMillis + 999
    val hours = ((millisAdjusted / 1000) / 3600).toInt()
    val minutes = ((millisAdjusted / 1000) % 3600 / 60).toInt()
    val seconds = ((millisAdjusted / 1000) % 60).toInt()
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%2d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

fun Calendar.getDateWithoutTime(date: Date): Date {
    time = date
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return time
}