package com.delta.restart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot Completed - Starting Foreground Service")
            val serviceIntent = Intent(context, SensorService::class.java)
            context.startForegroundService(serviceIntent)  // Start the foreground service after boot
        }
    }
}
