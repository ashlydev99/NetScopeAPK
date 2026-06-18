package cu.ashlydev.buzon

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import cu.ashlydev.buzon.service.CallReceiver

class NetScopeApp : Application() {
    
    companion object {
        private const val TAG = "NetScopeApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerCallReceiver()
        Log.d(TAG, "Aplicación iniciada")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "buzon_voz_channel",
                "Buzón de voz",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para el servicio de buzón de voz"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun registerCallReceiver() {
        try {
            val receiver = CallReceiver()
            val filter = IntentFilter().apply {
                addAction("android.intent.action.PHONE_STATE")
                addAction("android.intent.action.NEW_OUTGOING_CALL")
                priority = 1000
            }
            registerReceiver(receiver, filter)
            Log.d(TAG, "CallReceiver registrado dinámicamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar receiver: ${e.message}")
        }
    }
}