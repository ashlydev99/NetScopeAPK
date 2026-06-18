package cu.ashlydev.buzon.service

import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import cu.ashlydev.buzon.data.SettingsRepository

class IncomingCallService : InCallService() {
    private lateinit var settings: SettingsRepository
    private var currentCall: Call? = null
    private var callService: CallService? = null
    
    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        Log.d("IncomingCallService", "✅ Servicio InCall creado")
    }
    
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("IncomingCallService", "📞 onCallAdded: ${call.state}")
        
        // Solo procesar llamadas entrantes (RINGING) o que están en espera
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
            Log.d("IncomingCallService", "Intentando contestar llamada de: $phoneNumber")
            
            // Método oficial para Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ usa InCallService con VIDEO_STATE_AUDIO_ONLY
                call.answer(Call.VIDEO_STATE_AUDIO_ONLY)
                Log.d("IncomingCallService", "✅ Llamada contestada con InCallService")
            } else {
                // Fallback para Android 8
                val method = call.javaClass.getMethod("answer")
                method.invoke(call)
                Log.d("IncomingCallService", "✅ Llamada contestada con reflexión")
            }
            
            // Notificar a CallService que la llamada fue contestada
            val serviceIntent = android.content.Intent(this, CallService::class.java)
            startService(serviceIntent)
            
            // Llamar al método onCallAnswered de CallService
            val callServiceInstance = CallService()
            // No podemos llamar directamente, pero podemos usar un Broadcast o Intent
            // Para simplificar, usamos un Intent con acción personalizada
            val broadcastIntent = android.content.Intent("CALL_ANSWERED").apply {
                putExtra("phone_number", phoneNumber)
            }
            sendBroadcast(broadcastIntent)
            Log.d("IncomingCallService", "📢 Broadcast enviado a CallService")
            
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