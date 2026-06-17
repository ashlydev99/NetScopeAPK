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
        
        // Solo procesar llamadas entrantes
        if (call.state == Call.STATE_RINGING) {
            currentCall = call
            settings = SettingsRepository(this)
            
            // Obtener el número de teléfono (si está disponible)
            val details = call.details
            val phoneNumber = details.handle?.schemeSpecificPart ?: "Desconocido"
            
            Log.d("IncomingCallService", "Llamada entrante de: $phoneNumber")
            
            // Esperar el tiempo configurado y contestar
            val waitTime = settings.getWaitTime() * 1000L
            android.os.Handler(mainLooper).postDelayed({
                answerCall(call, phoneNumber)
            }, waitTime)
        }
    }
    
    private fun answerCall(call: Call, phoneNumber: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ usa InCallService
                call.answer(Call.VIDEO_STATE_AUDIO_ONLY)
            } else {
                // Fallback para Android 8
                val method = call.javaClass.getMethod("answer")
                method.invoke(call)
            }
            
            Log.d("IncomingCallService", "Llamada contestada automáticamente")
            
            // Iniciar el servicio que manejará la grabación
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