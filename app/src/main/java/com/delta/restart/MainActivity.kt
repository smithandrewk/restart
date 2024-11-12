package com.delta.restart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize FileHandler
        FileManager.initialize(this)

        log("MainActivity::onCreate")
        super.onCreate(savedInstanceState)

        val viewModel = MainViewModel(WalkingRepository(getSharedPreferences("my_prefs", MODE_PRIVATE)))

        setContent {
            WearApp(viewModel)
        }
    }

    override fun onStart() {
        log("MainActivity::onStart")
        super.onStart()
        if (!SensorService.isRunning) {
            log("MainActivity::sensor service was not running, starting now")
            val serviceIntent = Intent(this, SensorService::class.java)
            startForegroundService(serviceIntent)
        }
    }

    override fun onPause() {
        log("MainActivity::onPause")
        super.onPause()
    }

    override fun onDestroy() {
        log("MainActivity::onDestroy")
        super.onDestroy()
    }

    override fun onStop() {
        log("MainActivity::onStop")
        super.onStop()
    }
}
