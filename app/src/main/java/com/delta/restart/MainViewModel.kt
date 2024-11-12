package com.delta.restart

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(private val repository: WalkingRepository): ViewModel() {

    // Expose isWalking from the repository
    val isWalking: StateFlow<Boolean> get() = repository.isWalking

    // Function to update walking status
    fun setWalkingStatus(walking: Boolean) {
        log("Updating walking status from ${!walking} to $walking")
        repository.setWalkingStatus(walking)
    }
}