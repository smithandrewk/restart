package com.delta.restart

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi

class SensorService: Service() {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var mSensorHandler: SensorHandler
    private lateinit var mFileHandler: FileHandler
    private lateinit var mBatteryHandler: BatteryHandler
    private val mMainViewModel = MainViewModel()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        Log.d("0000","onCreateService")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorService::WakeLock");
        wakeLock.acquire()
        mFileHandler = FileHandler(filesDir)
        mSensorHandler = SensorHandler(mFileHandler,getSystemService(SENSOR_SERVICE) as SensorManager)
        if (Build.VERSION.SDK_INT > 28) {
            mBatteryHandler = BatteryHandler(::registerReceiver,::unregisterReceiver, mFileHandler, mMainViewModel::updateBatteryLevel)
        }
        startForegroundService()
    }

    private fun startForegroundService() {
        val channel = NotificationChannel(
            "my_service",
            "My Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        val notification: Notification = Notification.Builder(this, "my_service")
            .setContentTitle("Service Running")
            .setContentText("This is a running foreground service")
            .setSmallIcon(R.drawable.ic_notification)
            .build()


        startForeground(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onDestroy() {
        super.onDestroy()
        Log.d("0000","onDestroyService")
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        mSensorHandler.unregisterAll()
        mFileHandler.closeFiles()
        if (Build.VERSION.SDK_INT > 28) {
            mBatteryHandler.unregister()
        }
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}