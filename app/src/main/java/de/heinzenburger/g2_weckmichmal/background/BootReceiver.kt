package de.heinzenburger.g2_weckmichmal.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import kotlin.concurrent.thread

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            thread {
                val core = Core(context)
                core.log(Logger.Level.INFO, "Received Boot Event")
                core.runUpdateLogic()
            }
        }
    }
}