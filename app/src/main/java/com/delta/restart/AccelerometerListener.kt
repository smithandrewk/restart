package com.delta.restart

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

class AccelerometerListener (fileHandler: FileHandler, module: Module): SensorEventListener {
    private val dataBuffer = ArrayDeque<List<Float>>()
    private val windowSize = 1000
    private val mModule: Module = module
    private val mFileHandler = fileHandler

    override fun onSensorChanged(p0: SensorEvent) {
        dataBuffer.add(p0.values.toList())
        if (dataBuffer.size == windowSize) {
            val inputTensor = Tensor.fromBlob(dataBuffer.flatten().toFloatArray(), longArrayOf(1,3,1000))
            val inferenceResult = runModelInference(inputTensor)
            mFileHandler.writeToLog("inference,${inferenceResult.joinToString()},${p0.timestamp}")
            Log.d("0000",inferenceResult.joinToString())
            dataBuffer.clear()
        }
        mFileHandler.writeAccelerometerEvent(p0)
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    private fun runModelInference(inputTensor: Tensor): FloatArray {
        val outputTensor = mModule.forward(IValue.from(inputTensor)).toTensor()
        return outputTensor.dataAsFloatArray
    }
}