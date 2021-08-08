package com.codinginflow.just10minutes2.common.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codinginflow.just10minutes2.common.data.entities.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class TimerPreferences(val activeTaskId: Long)

@Singleton
class TimerPreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")
    private val dataStore = context.dataStore

    val timerPreferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val activeTaskId = preferences[PreferencesKeys.ACTIVE_TASK_ID] ?: Task.ID_NONE
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