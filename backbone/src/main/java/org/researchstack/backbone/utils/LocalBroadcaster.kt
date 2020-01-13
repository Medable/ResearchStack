package org.researchstack.backbone.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

internal object LocalBroadcaster {

    /**
     * Register Local Broadcast Manager with the receiver
     */
    fun registerReceiver(context: Context, broadcastReceiver: BroadcastReceiver?, action : String) {
        broadcastReceiver?.let {
            val intentFilter = IntentFilter()
            intentFilter.addAction(action)
            LocalBroadcastManager.getInstance(context).registerReceiver(it, intentFilter)
        }
    }

    /**
     * Unregister Local Broadcast Manager from the receiver
     */
    fun unregisterReceiver(context: Context, broadcastReceiver: BroadcastReceiver?) {
        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(it)
        }
    }
}