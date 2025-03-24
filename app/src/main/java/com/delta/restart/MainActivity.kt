package com.delta.restart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val REQUEST_FINE_LOCATION = 1
const val REQUEST_BACKGROUND_LOCATION = 2

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

        requestLocationPermissions()
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

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
        } else {
            // Fine location permission is granted, now request background location
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION)
            } else {
                if (!LocationService.isRunning) {
                    log("MainActivity::location service was not running, starting now")
                    val serviceIntent = Intent(this, LocationService::class.java)
                    startForegroundService(serviceIntent)
                }
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        log("onRequestPermissionsResult")
        when (requestCode) {
            REQUEST_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("permission fine location granted")
                    requestBackgroundLocationPermission()
                } else {
                    // Handle permission denied
                    finish()
                }
            }
            REQUEST_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Background location permission granted
                    log("background location permitted")
                    if (!LocationService.isRunning) {
                        log("MainActivity::location service was not running, starting now")
                        val serviceIntent = Intent(this, LocationService::class.java)
                        startForegroundService(serviceIntent)
                    }
                } else {
                    // Handle permission denied
                    finish()
                }
            }
        }
    }
}
