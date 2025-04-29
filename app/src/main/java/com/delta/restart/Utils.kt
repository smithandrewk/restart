package com.delta.restart

import android.util.Log
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity

fun log(msg: String) {
    Log.d("0000",msg)
    FileManager.writeToLog(msg)
}

object LocationUtils {
    const val REQUEST_FINE_LOCATION = 1
    const val REQUEST_BACKGROUND_LOCATION = 2

    fun checkAndRequestPermissions(context: Context, activity: ComponentActivity) {
        log("LocationUtils::checkAndRequestPermissions")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            log("LocationUtils::access fine location not granted. requesting fine location permission")
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
        } else {
            log("LocationUtils::access fine location already granted. checking background location permission")
            requestBackgroundLocationPermission(context, activity)
        }
    }

    private fun requestBackgroundLocationPermission(context: Context, activity: ComponentActivity) {
        log("LocationUtils::requestBackgroundLocationPermission")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                log("LocationUtils::access background location not granted. requesting background location permission")
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION)
            } else {
                log("LocationUtils::access background location already granted. startLocationServiceIfNotRunning")
                startLocationServiceIfNotRunning(context)
            }
        } else {
            log("LocationUtils::sdk too low for background location")
            startLocationServiceIfNotRunning(context)
        }
    }
    fun startLocationServiceIfNotRunning(context: Context) {
        if (!LocationService.isRunning) {
            val serviceIntent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            log("LocationUtils::LocationService is already running")
        }
    }

    fun handlePermissionResult(
        context: Context,
        requestCode: Int,
        grantResults: IntArray,
        onPermissionDenied: () -> Unit
    ) {
        when (requestCode) {
            REQUEST_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("LocationUtils::Fine location permission granted")
                    checkAndRequestPermissions(context, context as ComponentActivity)
                } else {
                    log("LocationUtils::Fine location permission denied")
                    onPermissionDenied()
                }
            }
            REQUEST_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("LocationUtils::Background location permission granted")
                    startLocationServiceIfNotRunning(context)
                } else {
                    log("LocationUtils::Background location permission denied")
                    onPermissionDenied()
                }
            }
        }
    }
}

object ServiceUtils {
    fun startSensorServiceIfNotRunning(context: Context) {
        if (!SensorService.isRunning) {
            val serviceIntent = Intent(context, SensorService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            log("ServiceUtils::SensorService is already running")
        }
    }
}