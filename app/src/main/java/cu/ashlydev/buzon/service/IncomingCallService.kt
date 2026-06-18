package cu.ashlydev.buzon.service

import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import cu.ashlydev.buzon.data.SettingsRepository

class IncomingCallService : InCallService() {
    private lateinit var settings: SettingsRepository
    private var currentCall: Call? = null
    
    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        Log.d("IncomingCallService", "✅ Servicio InCall creado")
    }
    
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("IncomingCallService", "📞 onCallAdded: ${call.state}")
        
        // Solo procesar llamadas entrantes
        if (call.state == Call.STATE_RINGING) {
            currentCall = call
            
            val details = call.details
            val phoneNumber = details.handle?.schemeSpecificPart ?: "Desconocido"
            Log.d("IncomingCallService", "📞 Llamada entrante de: $phoneNumber")
            
            // Obtener tiempo de espera
            val waitTime = settings.getWaitTime() * 1000L
            Log.d("IncomingCallService", "⏱️ Esperando $waitTime ms antes de contestar")
            
            // Contestar después del tiempo configurado
            android.os.Handler(mainLooper).postDelayed({
                answerCall(call, phoneNumber)
            }, waitTime)
        }
    }
    
    private fun answerCall(call: Call, phoneNumber: String) {
        try {
            Log.d("IncomingCallService", "📞 Intentando contestar llamada de: $phoneNumber")
            
            // Método compatible con Android 10+
            // En Android 10, VIDEO_STATE_AUDIO_ONLY no está disponible,
            // usamos 0 que significa "audio only" en todas las versiones
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ - usar 0 para audio only
                call.answer(0)
                Log.d("IncomingCallService", "✅ Llamada contestada con InCallService (audio only)")
            } else {
                // Fallback para Android 8 y menor
                val method = call.javaClass.getMethod("answer")
                method.invoke(call)
                Log.d("IncomingCallService", "✅ Llamada contestada con reflexión")
            }
            
            // NOTIFICAR A CALLSERVICE QUE LA LLAMADA FUE CONTESTADA
            Log.d("IncomingCallService", "📢 Notificando a CallService que la llamada fue contestada")
            
            // Opción 1: Usar broadcast
            val broadcastIntent = android.content.Intent("CALL_ANSWERED").apply {
                putExtra("phone_number", phoneNumber)
            }
            sendBroadcast(broadcastIntent)
            
            // Opción 2: Iniciar CallService si no está corriendo
            val serviceIntent = android.content.Intent(this, CallService::class.java).apply {
                putExtra("phone_number", phoneNumber)
                putExtra("call_answered", true)
            }
            startService(serviceIntent)
            
            Log.d("IncomingCallService", "✅ Notificación enviada")
            
        } catch (e: Exception) {
            Log.e("IncomingCallService", "❌ Error al contestar: ${e.message}")
        }
    }
    
    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("IncomingCallService", "📞 Llamada removida")
        if (currentCall == call) {
            currentCall = null
        }
    }
}