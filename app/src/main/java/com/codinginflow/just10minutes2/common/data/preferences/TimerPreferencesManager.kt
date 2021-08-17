package com.codinginflow.just10minutes2.common.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class TimerPreferences(val activeTaskId: Long?)

@Singleton
class TimerPreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_preferences")
    private val dataStore = context.dataStore

    val timerPreferencesFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading timer preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // TODO: 17.08.2021 Set default value for id to 1 before shipping
            val activeTaskId = preferences[PreferencesKeys.ACTIVE_TASK_ID]
            TimerPreferences(activeTaskId)
        }

    suspend fun updateActiveTaskId(id: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_TASK_ID] = id
        }
    }

    private object PreferencesKeys {
        val ACTIVE_TASK_ID = longPreferencesKey("active_task_id")
    }
}