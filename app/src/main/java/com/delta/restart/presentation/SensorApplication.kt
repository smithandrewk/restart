package com.delta.restart.presentation

import android.app.Application
import android.util.Log
import com.example.delta.util.FileHandler

class SensorApplication : Application() {

    lateinit var fileHandler: FileHandler
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("0000","SensorApplication::onCreate")
        Log.d("0000",filesDir.toString())
        fileHandler = FileHandler(filesDir)
    }
}
