package com.delta.restart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            log("BootReceiver::onReceive - Boot completed")
                ServiceUtils.startSensorServiceIfNotRunning(context)
                LocationUtils.checkAndRequestPermissions(context, context as ComponentActivity)
        }
    }
}
