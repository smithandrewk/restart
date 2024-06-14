package com.delta.restart.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.delta.util.FileHandler

class MainActivity : ComponentActivity() {
    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d("0000","onCreate")
        setTheme(android.R.style.Theme_DeviceDefault)

        fileHandler = (application as SensorApplication).fileHandler

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
        fileHandler.writeToLog("MainActivity::onDestroy")
        Log.d("0000","onDestroy")
        stopService()
    }
    override fun onResume() {
        super.onResume()
        fileHandler.writeToLog("MainActivity::onResume")
        Log.d("0000","onResume")

    }
    override fun onStart() {
        super.onStart()
        fileHandler.writeToLog("MainActivity::onStart")
        Log.d("0000","onStart")

    }
    override fun onPause() {
        super.onPause()
        fileHandler.writeToLog("MainActivity::onPause")
        Log.d("0000","onPause")
    }
}