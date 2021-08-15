package com.codinginflow.just10minutes2.common.util

import java.util.*

// TODO: 15.08.2021 Stunden mit rein formatieren
fun formatTimeText(timeInMillis: Long): String {
    val millisAdjusted = if (timeInMillis < 0) 0 else timeInMillis + 999
    val minutes = ((millisAdjusted / 1000) / 60).toInt()
    val seconds = ((millisAdjusted / 1000) % 60).toInt()
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

fun Calendar.getDateWithoutTime(date: Date): Date {
    time = date
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return time
}