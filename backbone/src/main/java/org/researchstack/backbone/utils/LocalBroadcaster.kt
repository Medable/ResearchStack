package org.researchstack.backbone.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

class LocalBroadcaster(private val context: Context,  lifecycleOwner: LifecycleOwner) : LifecycleObserver {

    var updatedColor = MutableLiveData<Int>()
    companion object Constant {
        const val OBJECT = "object"
        const val STATUS_BAR_COLOR_CHANGE = "status_bar_color_change"
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(ON_START)
    fun start() {
        val filter = IntentFilter()
        filter.addAction(STATUS_BAR_COLOR_CHANGE)
        StickyLocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
    }

    @OnLifecycleEvent(ON_STOP)
    fun stop() = StickyLocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val statusBarColor = intent?.extras?.get(OBJECT) as Int
            updatedColor.postValue(statusBarColor)
        }
    }
}