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
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SensorService: Service() {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var mSensorHandler: SensorHandler
    private lateinit var mFileHandler: FileHandler
    private lateinit var mBatteryHandler: BatteryHandler
    private lateinit var mModule: Module
    private val mMainViewModel = MainViewModel()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        Log.d("0000","onCreateService")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorService::WakeLock");
        wakeLock.acquire()
        mFileHandler = FileHandler(filesDir)
        mModule = loadModule()
        mSensorHandler = SensorHandler(mFileHandler,getSystemService(SENSOR_SERVICE) as SensorManager,mModule)
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
    private fun loadModule(): Module {
        val assetName = "model.ptl"
        var moduleFileAbsoluteFilePath: String = ""
        val moduleFile = File(this.filesDir, assetName)   // File on watch
        try {
            this.assets.open(assetName).use { `is` -> // Read from module file in "assets" dir
                FileOutputStream(moduleFile).use { os ->    // Write to file on watch
                    val buffer = ByteArray(4 * 1024)
                    while (true) {
                        val length = `is`.read(buffer)
                        if (length <= 0)
                            break
                        os.write(buffer, 0, length)     // Write module to file on watch
                    }
                    os.flush()
                    os.close()
                }
                moduleFileAbsoluteFilePath = moduleFile.absolutePath    // Path to file on watch
            }
        } catch (e: IOException) {
            Log.e("Main", "Error process asset $assetName to file path")
        }
        return LiteModuleLoader.load(moduleFileAbsoluteFilePath)
    }

}