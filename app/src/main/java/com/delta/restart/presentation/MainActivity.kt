package com.delta.restart.presentation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.example.delta.util.FileHandler
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    private var lastDirectorySize = mutableStateOf<Float?>(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        Log.d("0000","onCreate")

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(lastDirectorySize.value)
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
        lifecycleScope.launch {
            val size = getLastDirectorySize(this@MainActivity)
            lastDirectorySize.value = size
        }
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
@Composable
fun WearApp(lastDirectorySize: Float?) {
    val df = DecimalFormat("#.######")
    val formattedSize = lastDirectorySize?.let { df.format(it) }
    RestartTheme {
        Scaffold(
            timeText = { TimeText() },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Card(modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                        contentColor = MaterialTheme.colors.surface,
                        onClick = { },
                        content = {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Today",
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formattedSize?.let { "$ $it" } ?: "No directories found",
                                color = Color.Green
                            )
                        }
                    )
                }
            }
        )
    }
}

fun getLastDirectorySize(context: Context): Float {
    val filesDir = context.filesDir
    val directories = filesDir.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()

    // Sort directories by name
    val sortedDirectories = directories.sortedBy { it.name }

    val lastDirectory = sortedDirectories.lastOrNull()
    val sizeInBytes = lastDirectory?.let { getDirectorySize(it) } ?: 0L
    Log.d("0000","$sizeInBytes")
    return sizeInBytes / (1024f * 1024f) * 0.02f// Convert bytes to megabytes
}

fun getDirectorySize(directory: File): Long {
    var size = 0L
    directory.listFiles()?.forEach { file ->
        size += if (file.isDirectory) {
            getDirectorySize(file)
        } else {
            file.length()
        }
    }
    return size
}
class MainViewModel(): ViewModel() {
    private var currentBatteryLevel by mutableFloatStateOf(0f)
    fun updateBatteryLevel(newLevel: Float) {
        currentBatteryLevel = newLevel
    }
}