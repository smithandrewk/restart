package com.delta.restart

import android.content.Context
import android.hardware.SensorEvent
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.GZIPOutputStream

object FileManager {
    private lateinit var fLog: FileOutputStream
    private lateinit var fAccelerometerData: GZIPOutputStream
    private lateinit var fGyroscopeData: GZIPOutputStream
    private lateinit var filesDir: File
    private val appStartTimeReadable: String = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())

    // Initialize method to set up file streams and metadata
    fun initialize(context: Context) {
        if (!this::filesDir.isInitialized) {
            filesDir = context.filesDir
            setupDirectoriesAndFiles()
            log("FileManager was not initialized, initializing now")
        }
    }

    // Set up directories and initialize file streams
    private fun setupDirectoriesAndFiles() {
        // Create the log directory based on the app start time
        File(filesDir, appStartTimeReadable).mkdir()

        // Initialize the file streams
        fLog = FileOutputStream(File(filesDir, "$appStartTimeReadable/log.csv"), true)
        fAccelerometerData = GZIPOutputStream(FileOutputStream(File(filesDir, "$appStartTimeReadable/accelerometer_data.gz")))
        fAccelerometerData.write("ns_since_reboot,x,y,z\n".toByteArray())
        fGyroscopeData = GZIPOutputStream(FileOutputStream(File(filesDir, "$appStartTimeReadable/gyroscope_data.gz")))
        fGyroscopeData.write("ns_since_reboot,x,y,z\n".toByteArray())

        // Write metadata to the log file
        writeMetadata()
    }

    private fun writeMetadata() {
        val deviceModel = Build.MODEL
        val sdkVersion = Build.VERSION.SDK_INT
        val manufacturer = Build.MANUFACTURER
        fLog.write("Log Start Time: $appStartTimeReadable\n".toByteArray())
        fLog.write("Device Model: $deviceModel\n".toByteArray())
        fLog.write("Manufacturer: $manufacturer\n".toByteArray())
        fLog.write("SDK Version: $sdkVersion\n\n".toByteArray())
        fLog.write("Timestamp,Message\n".toByteArray())
    }

    fun writeToLog(msg: String) {
        if (this::fLog.isInitialized) {
            fLog.write("${System.currentTimeMillis()},$msg\n".toByteArray())
        } else {
            throw IllegalStateException("FileOutputStream is not initialized. Call initialize() first.")
        }
    }

    fun writeAccelerometerEvent(event: SensorEvent){
        fAccelerometerData.write("${event.timestamp},${event.values[0]},${event.values[1]},${event.values[2]}\n".toByteArray())
    }

    fun writeGyroscopeEvent(event: SensorEvent){
        fGyroscopeData.write("${event.timestamp},${event.values[0]},${event.values[1]},${event.values[2]}\n".toByteArray())
    }

    fun closeFileOutputStream() {
        log("closing file output streams")
        if (this::fLog.isInitialized) {
            fLog.close()
            fAccelerometerData.close()
            fGyroscopeData.close()
        }
    }
}