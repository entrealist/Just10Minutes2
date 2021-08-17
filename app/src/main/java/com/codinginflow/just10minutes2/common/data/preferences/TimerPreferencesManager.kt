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
import javax.inject.Inject
import javax.inject.Singleton

data class TimerPreferences(val selectedTaskId: Long?)

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
            val selectedTaskId = preferences[PreferencesKeys.SELECTED_TASK_ID]
            TimerPreferences(selectedTaskId)
        }

    suspend fun updateSelectedTaskId(id: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TASK_ID] = id
        }
    }

    private object PreferencesKeys {
        val SELECTED_TASK_ID = longPreferencesKey("selected_task_id")
    }
}