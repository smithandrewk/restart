package com.delta.restart

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService: Service() {
    companion object {
        var isRunning = false
        var fine_access = false
        var background_location = false
        var gettingLocationUpdates = false
    }
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var currentLocation by mutableStateOf<Location?>(null)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("LocationService::onStartCommand")
        if (checkPermissions()) { // this should be redundant
            log("LocationService::onStartCommand checkPermissions true")
            startLocationUpdates()
        } else {
            log("LocationService::onStartCommand checkPermissions false")
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("LocationService::onCreate")
        isRunning = true
        FileManager.initialize(this)
        startForegroundService()
    }
    private fun startForegroundService() {
        log("LocationService::startForegroundService")
        val channel = NotificationChannel(
            "location_service",
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        val notification: Notification = Notification.Builder(this, "location_service")
            .setContentTitle("Service Running")
            .setContentText("This is a running foreground service")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        startForeground(1, notification)
    }
    private fun startLocationUpdates() {
        log("LocationService::startLocationUpdates")
        gettingLocationUpdates = true

        locationRequest = LocationRequest.Builder(10000L).setMinUpdateIntervalMillis(5000L).setPriority(
            Priority.PRIORITY_HIGH_ACCURACY).build()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                log("LocationService::onLocationResult")
                locationResult.lastLocation?.let { location ->
                    // Handle the location here
                    val latitude = location.latitude
                    val longitude = location.longitude
                    log("latitiude $latitude")
                    log("longitude $longitude")
                    currentLocation = locationResult.lastLocation
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                log("LocationService::onLocationAvailability")
                if (!locationAvailability.isLocationAvailable) {
                    // Location services are not available
                    log("location services not available")
                }
                log("location services available")
            }
        }
        if (checkPermissions()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
    private fun stopLocationUpdates() {
        log("LocationService::stopLocationUpdates")
        gettingLocationUpdates = false
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            log("LocationService::checkPermissions ACCESS_FINE_LOCATION not granted")
        } else if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            log("LocationService::checkPermissions ACCESS_BACKGROUND_LOCATION not granted")
        } else {
            log("LocationService::checkPermissions permissions granted")
            return true
        }
        return false
    }
    override fun onDestroy() {
        log("LocationService::onDestroy")
        super.onDestroy()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        log("LocationService::onBind")

        return null
    }
}