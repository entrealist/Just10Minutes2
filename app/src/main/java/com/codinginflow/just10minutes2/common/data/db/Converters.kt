package com.codinginflow.just10minutes2.common.data.db

import androidx.room.TypeConverter
import com.codinginflow.just10minutes2.R
import com.codinginflow.just10minutes2.common.data.entities.WeekdaySelection

class Converters {
    @TypeConverter
    fun stringToWeekdaySelection(weekdaysString: String?): WeekdaySelection? {
        return weekdaysString?.let {
            WeekdaySelection(
                mondayActive = weekdaysString.contains("MO"),
                tuesdayActive = weekdaysString.contains("TU"),
                wednesdayActive = weekdaysString.contains("WE"),
                thursdayActive = weekdaysString.contains("TH"),
                fridayActive = weekdaysString.contains("FR"),
                saturdayActive = weekdaysString.contains("SA"),
                sundayActive = weekdaysString.contains("SU")
            )
        }
    }

    @TypeConverter
    fun weekdaySelectionToString(weekdaySelection: WeekdaySelection?): String? {
        return weekdaySelection?.let {
            val weekdaysString = StringBuilder()
            if (it.mondayActive) weekdaysString.append("MO ")
            if (it.tuesdayActive) weekdaysString.append("TU ")
            if (it.wednesdayActive) weekdaysString.append("WE ")
            if (it.thursdayActive) weekdaysString.append("TH ")
            if (it.fridayActive) weekdaysString.append("FR ")
            if (it.saturdayActive) weekdaysString.append("SA ")
            if (it.sundayActive) weekdaysString.append("SU ")
            return weekdaysString.toString()
        }
    }
}