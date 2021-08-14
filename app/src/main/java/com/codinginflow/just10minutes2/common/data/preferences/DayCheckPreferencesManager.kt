package com.codinginflow.just10minutes2.common.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codinginflow.just10minutes2.common.util.getDateWithoutTime
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayCheckPreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "day_check_preferences")
    private val dataStore = context.dataStore

    val activeDay = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading day check preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val activeDayTimestamp = preferences[PreferencesKeys.ACTIVE_DAY_TIMESTAMP] ?: 0
            Date(activeDayTimestamp)
        }

    suspend fun updateActiveDay(date: Date) {
        val timestamp = Calendar.getInstance().getDateWithoutTime(date).time
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_DAY_TIMESTAMP] = timestamp
        }
    }

    private object PreferencesKeys {
        val ACTIVE_DAY_TIMESTAMP = longPreferencesKey("active_day")
    }
}