package com.delta.restart.presentation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import com.delta.restart.R
import com.delta.restart.presentation.theme.RestartTheme
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.delta.util.FileHandler

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        Log.d("0000","onCreate")

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }

        startService()
    }

    private fun startService() {
        val serviceIntent = Intent(this, SensorService::class.java)
        startService(serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(this, SensorService::class.java)
        stopService(serviceIntent)
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("0000","onDestroy")
        stopService()
    }

    override fun onResume() {
        super.onResume()
        Log.d("0000","onResume")

    }

    override fun onStart() {
        super.onStart()
        Log.d("0000","onStart")

    }

    override fun onPause() {
        super.onPause()
        Log.d("0000","onPause")
    }
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class SensorService: Service() {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var mSensorHandler: SensorHandler
    private lateinit var mFileHandler: FileHandler
    private lateinit var mBatteryHandler: BatteryHandler
    private val mMainViewModel = MainViewModel()

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        Log.d("0000","onCreateService")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorService::WakeLock");
        wakeLock.acquire()
        mFileHandler = FileHandler(filesDir)
        mSensorHandler = SensorHandler(mFileHandler,getSystemService(SENSOR_SERVICE) as SensorManager)
        mBatteryHandler = BatteryHandler(::registerReceiver,::unregisterReceiver, mFileHandler, mMainViewModel::updateBatteryLevel)

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

    override fun onDestroy() {
        super.onDestroy()
        Log.d("0000","onDestroyService")
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        mSensorHandler.unregisterAll()
        mFileHandler.closeFiles()
        mBatteryHandler.unregister()
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}
@Composable
fun WearApp() {
    RestartTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
        }
    }
}

class MainViewModel(): ViewModel() {
    private var currentBatteryLevel by mutableFloatStateOf(0f)
    fun updateBatteryLevel(newLevel: Float) {
        currentBatteryLevel = newLevel
    }
}