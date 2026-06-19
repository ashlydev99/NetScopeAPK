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
        
        if (call.state == Call.STATE_RINGING) {
            currentCall = call
            
            val details = call.details
            val phoneNumber = details.handle?.schemeSpecificPart ?: "Desconocido"
            Log.d("IncomingCallService", "📞 Llamada entrante de: $phoneNumber")
            
            val waitTime = settings.getWaitTime() * 1000L
            Log.d("IncomingCallService", "⏱️ Esperando $waitTime ms")
            
            android.os.Handler(mainLooper).postDelayed({
                answerCall(call, phoneNumber)
            }, waitTime)
        }
    }
    
    private fun answerCall(call: Call, phoneNumber: String) {
        try {
            Log.d("IncomingCallService", "📞 Contestando llamada de: $phoneNumber")
            
            // Método oficial para Android 10+
            call.answer(0) // 0 = audio only
            Log.d("IncomingCallService", "✅ Llamada contestada")
            
            // Notificar a CallService
            val serviceIntent = android.content.Intent(this, CallService::class.java).apply {
                putExtra("phone_number", phoneNumber)
                putExtra("call_answered", true)
            }
            startService(serviceIntent)
            
            // También enviar broadcast por si acaso
            val broadcastIntent = android.content.Intent("CALL_ANSWERED").apply {
                putExtra("phone_number", phoneNumber)
            }
            sendBroadcast(broadcastIntent)
            
            Log.d("IncomingCallService", "✅ Notificación enviada a CallService")
            
        } catch (e: Exception) {
            Log.e("IncomingCallService", "❌ Error: ${e.message}")
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