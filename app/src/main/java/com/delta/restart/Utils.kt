package com.delta.restart

import android.util.Log

fun log(msg: String) {
    Log.d("0000",msg)
    FileManager.writeToLog(msg)
}