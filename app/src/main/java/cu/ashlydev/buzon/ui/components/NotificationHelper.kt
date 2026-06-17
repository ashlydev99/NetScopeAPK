package cu.ashlydev.buzon.ui.components

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
                description = "Notificaciones del buzón de voz"
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }
        
        val messageCount = MessageRepository.getAllMessages(context).size
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent para salir
        val exitIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("exit", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val exitPendingIntent = PendingIntent.getActivity(
            context,
            1,
            exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Buzón de voz")
            .setContentText("Mensajes nuevos: $messageCount")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Salir",
                exitPendingIntent
            )
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