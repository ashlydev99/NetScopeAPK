package cu.ashlydev.buzon.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        
        if (TelephonyManager.EXTRA_STATE_RINGING == state && number != null) {
            // Iniciar el servicio para contestar la llamada
            val serviceIntent = Intent(context, CallService::class.java).apply {
                putExtra("phone_number", number)
            }
            context.startService(serviceIntent)
        }
    }
}