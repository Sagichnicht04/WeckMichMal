package de.heinzenburger.g2_weckmichmal.background

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import de.heinzenburger.g2_weckmichmal.R
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import java.time.LocalDate
import kotlin.concurrent.thread


class AlarmEvent : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notification = NotificationCompat.Builder(context!!, "alarm_channel")
            .setContentTitle("Alarm")
            .setContentText("Wake up!")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)

        var configurationWithEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("configurationWithEvent", ConfigurationWithEvent::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<ConfigurationWithEvent>("configurationWithEvent")!!
        }

        val serviceIntent = Intent(context, ForegroundService::class.java)
        serviceIntent.putExtra("configurationWithEvent", configurationWithEvent)
        context.startService(serviceIntent)

        val core = Core(context)
        core.log(Logger.Level.SEVERE, "I rang hehe! UID: ${configurationWithEvent.configuration.uid}")

        thread {
            core.gameEventAlarmRinging()
            core.updateConfigurationIchHabGeringt(LocalDate.now(), configurationWithEvent.configuration.uid)
            core.runUpdateLogic()
        }
    }
}