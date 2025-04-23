package com.delta.restart

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper

class SensorHandler(sensorManager: SensorManager) {
    private var mSensorManager: SensorManager = sensorManager
    private var sensorsRegistered = false // Tracks if accelerometer and gyroscope are registered
    private var lastOffBodyValue: Float? = null // Stores the most recent off-body value
    private val handler = Handler(Looper.getMainLooper()) // Handler for delayed actions
    private var debounceRunnable: Runnable? = null // Reference to the current debounce task

    private val mAccelerometerListener: SensorListener = SensorListener { event -> FileManager.writeAccelerometerEvent(event) }
    private val mGyroscopeListener: SensorListener = SensorListener { event -> FileManager.writeGyroscopeEvent(event) }
    private val mOffBodyListener: SensorListener = SensorListener { event ->
        val currentValue = event.values[0]
        log("Off-body sensor value: $currentValue")

        // If the value hasn't changed, do nothing
        if (lastOffBodyValue == currentValue) return@SensorListener

        // Update the last value
        lastOffBodyValue = currentValue

        // Cancel any pending debounce action
        debounceRunnable?.let { handler.removeCallbacks(it) }

        // Schedule a new debounce action
        debounceRunnable = Runnable {
            if (currentValue == 0f) {
                log("Debounced: Off-body detected unregistering all sensors except off-body sensor")
                unregisterAllExceptOffBody()
            } else if (currentValue == 1f) {
                log("Debounced: On-body detected registering all sensors")
                registerAll()
            }
        }
        handler.postDelayed(debounceRunnable!!, 3000) // Delay of 3 seconds
    }

    init {
        registerOffBodySensor() // Ensure the off-body sensor is always registered
        registerAll() // Register other sensors initially
    }

    private fun registerAll() {
        if (!sensorsRegistered) {
            val samplingRateHertz = 100
            val mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            val samplingPeriodMicroseconds = 1000000 / samplingRateHertz
            log("Registering accelerometer and gyroscope")
            mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, samplingPeriodMicroseconds)
            mSensorManager.registerListener(mGyroscopeListener, mGyroscope, samplingPeriodMicroseconds)
            sensorsRegistered = true
        } else {
            log("Sensors are already registered")
        }
    }

    private fun registerOffBodySensor() {
        val mOffBody = mSensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT)
        log("Registering off-body sensor")
        mSensorManager.registerListener(mOffBodyListener, mOffBody, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unregisterAccelerometer() {
        mSensorManager.unregisterListener(mAccelerometerListener)
    }

    private fun unregisterGyroscope() {
        mSensorManager.unregisterListener(mGyroscopeListener)
    }

    private fun unregisterAllExceptOffBody() {
        if (sensorsRegistered) {
            log("Unregistering accelerometer and gyroscope keeping off-body sensor registered")
            unregisterAccelerometer()
            unregisterGyroscope()
            sensorsRegistered = false
        } else {
            log("Sensors are already unregistered")
        }
    }

    fun unregisterAll() {
        if (sensorsRegistered) {
            log("Unregistering all sensors including off-body sensor")
            unregisterAccelerometer()
            unregisterGyroscope()
            unregisterOffBody()
            sensorsRegistered = false
        } else {
            log("Sensors are already unregistered")
        }
    }

    private fun unregisterOffBody() {
        mSensorManager.unregisterListener(mOffBodyListener)
    }
}

class SensorListener(private val writeEvent: (SensorEvent) -> Unit) : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            writeEvent(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}