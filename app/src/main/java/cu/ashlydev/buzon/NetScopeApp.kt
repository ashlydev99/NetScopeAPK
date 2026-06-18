package cu.ashlydev.buzon

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * Clase Application para la aplicación Buzón de Voz.
 * Se encarga de inicializar componentes globales, como los canales de notificación.
 */
class NetScopeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializar el canal de notificaciones al arrancar la app
        createNotificationChannel()
    }

    /**
     * Crea el canal de notificaciones necesario para Android 8.0 (Oreo) y superiores.
     * Este canal es utilizado por el servicio en primer plano (CallService).
     */
    private fun createNotificationChannel() {
        // Solo es necesario crear el canal en Android 8.0 (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "buzon_voz_channel", // El ID del canal, debe coincidir con el usado en CallService
                "Buzón de voz", // El nombre visible para el usuario
                NotificationManager.IMPORTANCE_LOW // Importancia baja para no interrumpir
            ).apply {
                description = "Canal para el servicio de buzón de voz en primer plano"
            }

            // Registrar el canal en el sistema
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}