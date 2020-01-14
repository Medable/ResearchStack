package org.researchstack.backbone.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LocalBroadcaster(private val appContext: Context, lifecycleOwner: LifecycleOwner,
                       colorChangeCallback: (color: Int) -> Unit) : LifecycleObserver {
    private val OBJECT = "object"
    private val STATUS_BAR_COLOR_CHANGE = "status_bar_color_change"

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(ON_START)
    fun start() {
        val filter = IntentFilter()
        filter.addAction(STATUS_BAR_COLOR_CHANGE)
        LocalBroadcastManager.getInstance(appContext).registerReceiver(receiver, filter)
    }

    @OnLifecycleEvent(ON_DESTROY)
    fun stop() = LocalBroadcastManager.getInstance(appContext).unregisterReceiver(receiver)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val statusBarColor = intent?.extras?.get(OBJECT) as Int
            colorChangeCallback(statusBarColor)
        }
    }
}