package cu.netscope.pro.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import cu.netscope.pro.MainActivity
import cu.netscope.pro.R
import cu.netscope.pro.util.NotificationUtils
import kotlinx.coroutines.*

class NetMonitorService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var telephonyManager: TelephonyManager
    private var running = false

    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(TelephonyManager::class.java)
        startForeground(NotificationUtils.NOTIF_ID, buildNotification("?", "?", "?", null))
        running = true
        scope.launch {
            monitorLoop()
        }
    }

    private suspend fun monitorLoop() {
        while (running) {
            updateNotification()
            delay(3000)
        }
    }

    private fun updateNotification() {
        val operator = telephonyManager.networkOperatorName ?: "?"
        val networkType = when (telephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_UMTS -> "WCDMA"
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE -> "GSM"
            else -> "?"
        }
        var dbmText = "?"
        try {
            val all = telephonyManager.allCellInfo
            val primary = all?.firstOrNull { it.isRegistered } ?: all?.firstOrNull()
            val dbm = when (primary) {
                is android.telephony.CellInfoLte -> primary.cellSignalStrength?.dbm
                is android.telephony.CellInfoWcdma -> primary.cellSignalStrength?.dbm
                is android.telephony.CellInfoGsm -> primary.cellSignalStrength?.dbm
                else -> null
            }
            dbmText = dbm?.toString() ?: "?"
        } catch (e: SecurityException) {
            dbmText = "?"
        } catch (e: Exception) {
            dbmText = "?"
        }
        val band = "?"
        val notif = buildNotification(operator, networkType, band, dbmText)
        val nm = getSystemService(android.app.NotificationManager::class.java)
        nm?.notify(NotificationUtils.NOTIF_ID, notif)
    }

    private fun buildNotification(operator: String, networkType: String, band: String?, dbm: String?): Notification {
        val stopIntent = Intent(this, NetMonitorService::class.java).apply {
            action = "ACTION_STOP"
        }
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID)
            .setContentTitle(operator)
            .setContentText("$networkType • ${dbm ?: "?"} dBm • ${band ?: "?"}")
            .setSmallIcon(R.drawable.ic_signal_bars)
            .setContentIntent(openPending)
            .addAction(0, getString(R.string.notification_exit), stopPending)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_STOP") {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}