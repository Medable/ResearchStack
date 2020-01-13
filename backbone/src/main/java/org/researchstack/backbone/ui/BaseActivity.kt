package org.researchstack.backbone.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.researchstack.backbone.utils.LocalBroadcaster

open class BaseActivity : AppCompatActivity() {

    companion object {
        const val OBJECT = "object"
        const val STATUS_BAR_COLOR_CHANGE = "status_bar_color_change"
    }
    override fun onStart() {
        super.onStart()
        LocalBroadcaster.registerReceiver(this,receiver, STATUS_BAR_COLOR_CHANGE)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcaster.unregisterReceiver(this, receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val statusBarColor =intent?.extras?.get(OBJECT) as Int
            window.statusBarColor = statusBarColor
        }
    }
}
