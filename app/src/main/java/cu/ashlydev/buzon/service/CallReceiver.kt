package cu.ashlydev.buzon.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "android.intent.action.PHONE_STATE" -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                
                Log.d("CallReceiver", " Estado: $state, Número: $number")
                
                if (TelephonyManager.EXTRA_STATE_RINGING == state && number != null) {
                    Log.d("CallReceiver", " Llamada entrante de: $number")
                    
                    val serviceIntent = Intent(context, CallService::class.java).apply {
                        putExtra("phone_number", number)
                    }
                    context.startService(serviceIntent)
                }
            }
            "CALL_ANSWERED" -> {
                val number = intent.getStringExtra("phone_number") ?: ""
                Log.d("CallReceiver", " CALL_ANSWERED recibido: $number")
                
                // CallService ya está corriendo, solo notificar
                val serviceIntent = Intent(context, CallService::class.java)
                context.startService(serviceIntent)
            }
        }
    }
}