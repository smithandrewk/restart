package com.delta.restart

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHandler(fileHandler: FileHandler, sensorManager: SensorManager) {
    private var mSensorManager: SensorManager = sensorManager
    private var mFileHandler: FileHandler = fileHandler
    private val mAccelerometerListener: SensorListener = SensorListener { event -> mFileHandler.writeAccelerometerEvent(event) }
    private val mGyroscopeListener: SensorListener = SensorListener { event -> mFileHandler.writeGyroscopeEvent(event) }
    init {
        val samplingRateHertz = 100
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, samplingPeriodMicroseconds)
        mSensorManager.registerListener(mGyroscopeListener, mGyroscope, samplingPeriodMicroseconds)
    }
    private fun unregisterAccelerometer() {
        mSensorManager.unregisterListener(mAccelerometerListener)
    }
    private fun unregisterGyroscope() {
        mSensorManager.unregisterListener(mGyroscopeListener)
    }
    fun unregisterAll() {
        unregisterAccelerometer()
        unregisterGyroscope()
    }
}

class SensorListener (private val writeEvent: (SensorEvent) -> Unit): SensorEventListener {
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            writeEvent(event)
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}