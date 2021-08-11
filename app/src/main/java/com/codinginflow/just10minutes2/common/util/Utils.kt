package com.codinginflow.just10minutes2.common.util

import java.util.*

fun formatTimeText(timeInMillis: Long): String {
    val millisAdjusted = timeInMillis + 999
    val minutes = ((millisAdjusted / 1000) / 60).toInt()
    val seconds = ((millisAdjusted / 1000) % 60).toInt()
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}