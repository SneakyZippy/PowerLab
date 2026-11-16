package com.sneakyzippy.powerlab.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sneakyzippy.powerlab.core.engine.ChargingBenchmarkManager

class DismissNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ChargingBenchmarkManager.getInstance(context).clearPendingSession()
        ChargingNotificationHelper.cancelNotification(context)
    }
}
