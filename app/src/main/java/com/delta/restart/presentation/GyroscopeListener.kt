package com.delta.restart.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.example.delta.util.FileHandler

class GyroscopeListener (fileHandler: FileHandler): SensorEventListener {
    private val mFileHandler = fileHandler
    override fun onSensorChanged(p0: SensorEvent) {
        mFileHandler.writeGyroscopeEvent(p0)
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}