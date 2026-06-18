package cu.ashlydev.buzon.service

import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import cu.ashlydev.buzon.data.SettingsRepository

class IncomingCallService : InCallService() {
    private lateinit var settings: SettingsRepository
    private var currentCall: Call? = null
    
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        
        if (call.state == Call.STATE_RINGING) {
            currentCall = call
            settings = SettingsRepository(this)
            
            val details = call.details
            val phoneNumber = details.handle?.schemeSpecificPart ?: "Desconocido"
            
            Log.d("IncomingCallService", "Llamada de: $phoneNumber")
            
            val waitTime = settings.getWaitTime() * 1000L
            android.os.Handler(mainLooper).postDelayed({
                answerCall(call)
            }, waitTime)
        }
    }
    
    private fun answerCall(call: Call) {
        try {
            // Método oficial para Android 10
            call.answer(Call.VIDEO_STATE_AUDIO_ONLY)
            Log.d("IncomingCallService", "Llamada contestada")
            
            // Iniciar servicio de grabación
            val details = call.details
            val phoneNumber = details.handle?.schemeSpecificPart ?: "Desconocido"
            
            val serviceIntent = android.content.Intent(this, CallService::class.java).apply {
                putExtra("phone_number", phoneNumber)
            }
            startService(serviceIntent)
            
        } catch (e: Exception) {
            Log.e("IncomingCallService", "Error al contestar: ${e.message}")
        }
    }
    
    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (currentCall == call) {
            currentCall = null
        }
    }
}