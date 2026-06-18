package cu.ashlydev.buzon.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import cu.ashlydev.buzon.MainActivity
import cu.ashlydev.buzon.R
import cu.ashlydev.buzon.data.MessageRepository

object NotificationHelper {
    private const val CHANNEL_ID = "buzon_voz_channel"
    private const val NOTIFICATION_ID = 1001
    
    fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Buzón de voz",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio de buzón de voz activo"
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }
        
        val messageCount = MessageRepository.getAllMessages(context).size
        
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("exit", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val stopPendingIntent = PendingIntent.getActivity(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Buzón de voz")
            .setContentText("Mensajes: $messageCount")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, "Abrir", openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPendingIntent)
            .build()
        
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    fun updateNotification(context: Context) {
        showNotification(context)
    }
    
    fun cancelNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}