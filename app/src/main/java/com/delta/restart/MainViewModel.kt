package com.delta.restart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel(): ViewModel() {
    private var currentBatteryLevel by mutableFloatStateOf(0f)
    fun updateBatteryLevel(newLevel: Float) {
        currentBatteryLevel = newLevel
    }
}