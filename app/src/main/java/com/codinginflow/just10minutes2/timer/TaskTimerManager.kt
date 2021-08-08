package com.codinginflow.just10minutes2.timer

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskTimerManager @Inject constructor() {

    private val activeTimers = listOf<TaskTimer>()
}