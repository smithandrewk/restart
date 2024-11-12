package com.delta.restart

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WalkingRepository(private val sharedPreferences: SharedPreferences) {

    // Key for the walking status in SharedPreferences
    private val WALKING_STATUS_KEY = "is_walking"

    // StateFlow to hold the current walking status
    private val _isWalking = MutableStateFlow(getWalkingStatusFromPrefs())
    val isWalking: StateFlow<Boolean> get() = _isWalking

    // Read the initial walking status from SharedPreferences
    private fun getWalkingStatusFromPrefs(): Boolean {
        return sharedPreferences.getBoolean(WALKING_STATUS_KEY, false)
    }

    // Update both StateFlow and SharedPreferences
    fun setWalkingStatus(walking: Boolean) {
        _isWalking.value = walking
        sharedPreferences.edit().putBoolean(WALKING_STATUS_KEY, walking).apply()
    }
}