package com.delta.restart
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class BatteryHandler (registerReceiver: (receiver: BroadcastReceiver, filter: IntentFilter, flags: Int) -> Unit, unregisterReceiver: (br: BroadcastReceiver) -> Unit){
    private var lastChargingStatus: Boolean = false
    private val br: BroadcastReceiver = BatteryBroadcastReceiver()
    private val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    private val listenToBroadcastsFromOtherApps = false
    private val receiverFlags = if (listenToBroadcastsFromOtherApps) {
        ComponentActivity.RECEIVER_EXPORTED
    } else {
        ComponentActivity.RECEIVER_NOT_EXPORTED
    }
    private val mUnregisterReceiver = unregisterReceiver
    init {
        registerReceiver(br, filter, receiverFlags)
    }
    fun unregister(){
        mUnregisterReceiver(br)
    }
    inner class BatteryBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            if (isCharging != lastChargingStatus) {
                if (isCharging) {
                    log("Watch is now on charger.")
                } else {
                    log("Watch is now off charger.")
                }
                lastChargingStatus = isCharging
            }
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryLevel = level * 100 / scale.toFloat()
            log("battery: $batteryLevel")
        }
    }
}