package com.delta.restart

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHandler(sensorManager: SensorManager) {
    private var mSensorManager: SensorManager = sensorManager
    private val mAccelerometerListener: SensorListener = SensorListener { event -> FileManager.writeAccelerometerEvent(event) }
    private val mGyroscopeListener: SensorListener = SensorListener { event -> FileManager.writeGyroscopeEvent(event) }
    init {
        val samplingRateHertz = 100
        val mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val samplingPeriodMicroseconds = 1000000/samplingRateHertz
        log("registering sensor event listeners")
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