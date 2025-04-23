package com.delta.restart

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
        ServiceUtils.startSensorServiceIfNotRunning(this)
        LocationUtils.checkAndRequestPermissions(this, this)
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


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        log("MainActivity::onRequestPermissionsResult")
        LocationUtils.handlePermissionResult(
            this,
            requestCode,
            grantResults
        ) {
            // Handle permission denied
            log("MainActivity::Permission denied, finishing activity")
            finish()
        }
    }
}
