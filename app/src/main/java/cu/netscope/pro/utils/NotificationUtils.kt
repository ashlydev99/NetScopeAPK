package cu.netscope.pro.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import cu.netscope.pro.R

object NotificationUtils {
    const val CHANNEL_ID = "netscope_monitor_channel"
    const val NOTIF_ID = 1001

    fun createChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm != null) {
            val ch = NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW)
            ch.description = "Monitorea estado de red"
            nm.createNotificationChannel(ch)
        }
    }
}