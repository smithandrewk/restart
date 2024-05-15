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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.delta.util.FileHandler
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        Log.d("0000","onCreate")

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(::listDirectories,::tarGzDirectories,::startService,::stopService)
        }

        startService()
    }
    private fun listDirectories() {
        val root = File(getExternalFilesDir(null)!!.absolutePath)
        val files = root.listFiles()
        files?.filter { it.isDirectory }?.forEach { directory ->
            println("${directory.name} - Size: ${getDirectorySize(directory)} bytes")
        }
    }
    private fun getDirectorySize(directory: File): Long {
        var size: Long = 0
        val files = directory.listFiles()
        files?.forEach { file ->
            size += if (file.isDirectory) getDirectorySize(file) else file.length()
        }
        return size
    }
    private fun tarGzDirectories(context: Context): List<String> {
        val mainDirectory = context.getExternalFilesDir(null) ?: return emptyList()
        val subdirectories = mainDirectory.listFiles { file -> file.isDirectory } ?: return emptyList()

        val tarGzFilePaths = mutableListOf<String>()

        subdirectories.forEach { subdirectory ->
            try {
                val tarGzFile = File(subdirectory.parent, "${subdirectory.name}.tar.gz")
                FileOutputStream(tarGzFile).use { fos ->
                    GzipCompressorOutputStream(fos).use { gcos ->
                        TarArchiveOutputStream(gcos).use { taos ->
                            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
                            addFilesToTarGz(taos, subdirectory, "")
                        }
                    }
                }
                tarGzFilePaths.add(tarGzFile.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return tarGzFilePaths
    }
    @Throws(IOException::class)
    private fun addFilesToTarGz(tarArchive: TarArchiveOutputStream, file: File, parent: String) {
        val entryName = if (parent.isEmpty()) file.name else "$parent/${file.name}"
        val entry = TarArchiveEntry(file, entryName)

        tarArchive.putArchiveEntry(entry)

        if (file.isFile) {
            FileInputStream(file).use { fis ->
                BufferedInputStream(fis).use { bis ->
                    bis.copyTo(tarArchive, 8192)
                }
            }
            tarArchive.closeArchiveEntry()
        } else if (file.isDirectory) {
            tarArchive.closeArchiveEntry()
            file.listFiles()?.forEach { child ->
                addFilesToTarGz(tarArchive, child, entryName)
            }
        }
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
        mFileHandler = FileHandler(getExternalFilesDir(null)!!)
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
fun WearApp(listDirectories: () -> Unit, zip: (Context) -> Unit,startService: () -> Unit,stopService: () -> Unit,) {
    RestartTheme {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            val startTime = System.currentTimeMillis()
                            tryAwaitRelease()
                            val endTime = System.currentTimeMillis()
                            Log.d("0000","${endTime - startTime}")
                            if (endTime - startTime >= 5000) { // 5 seconds
                                scope.launch {
                                    Log.d("0000","pressed")
                                    stopService()
                                    listDirectories()
                                    zip(context)
                                }
                            }
                        }
                    )
                },
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