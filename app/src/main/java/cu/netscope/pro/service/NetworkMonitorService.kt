package cu.netscope.pro.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import cu.netscope.pro.MainActivity
import cu.netscope.pro.R
import cu.netscope.pro.data.model.CellInfo
import cu.netscope.pro.data.model.NetworkState
import java.util.concurrent.atomic.AtomicReference

class NetworkMonitorService : LifecycleService() {

    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var monitorHandler: Handler? = null
    private var monitorRunnable: Runnable? = null

    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastUpdateTime = System.currentTimeMillis()

    private val currentNetworkState = AtomicReference<NetworkState>(NetworkState())
    private val speedHistory = mutableListOf<Float>()
    private val maxSpeedHistory = 30

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "network_monitor_channel"
        const val ACTION_STOP = "cu.netscope.pro.STOP_SERVICE"
        private const val TAG = "NetScopeService"

        var networkStateListener: ((NetworkState) -> Unit)? = null

        fun startService(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio creado")
        createNotificationChannel()
        setupTelephonyMonitoring()
        startSpeedMonitoring()
        startForeground(NOTIFICATION_ID, buildNotification("Iniciando..."))
        monitorHandler?.postDelayed({ updateNetworkState() }, 500)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitoreo de Red",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra información de la red móvil en tiempo real"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(info: String): Notification {
        val stopIntent = Intent(this, NetworkMonitorService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NetScope Pro")
            .setContentText(info)
            .setSmallIcon(R.drawable.ic_signal)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Salir", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @Suppress("DEPRECATION")
    private fun setupTelephonyMonitoring() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        phoneStateListener = object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                updateNetworkState()
            }
            override fun onCellInfoChanged(cellInfo: MutableList<android.telephony.CellInfo>) {
                updateNetworkState()
            }
            override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                updateNetworkState()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED) {
            telephonyManager?.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                PhoneStateListener.LISTEN_CELL_INFO or
                PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED
            )
        }
    }

    private fun startSpeedMonitoring() {
        monitorHandler = Handler(Looper.getMainLooper())
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastUpdateTime = System.currentTimeMillis()

        monitorRunnable = object : Runnable {
            override fun run() {
                updateSpeedAndNotify()
                monitorHandler?.postDelayed(this, 1000L)
            }
        }
        monitorHandler?.post(monitorRunnable!!)
    }

    private fun updateSpeedAndNotify() {
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()
        val timeDelta = (currentTime - lastUpdateTime) / 1000f

        if (timeDelta > 0) {
            val rxSpeed = ((currentRxBytes - lastRxBytes) * 8f) / timeDelta
            val txSpeed = ((currentTxBytes - lastTxBytes) * 8f) / timeDelta
            val speedMbps = (rxSpeed + txSpeed) / 1_000_000f

            synchronized(speedHistory) {
                speedHistory.add(speedMbps)
                if (speedHistory.size > maxSpeedHistory) {
                    speedHistory.removeAt(0)
                }
            }

            val state = currentNetworkState.get()
            state.downloadSpeedBps = rxSpeed
            state.uploadSpeedBps = txSpeed
            state.speedHistory = speedHistory.toList()
            currentNetworkState.set(state)
            networkStateListener?.invoke(state)
        }

        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
        lastUpdateTime = currentTime
        updateNotification()
    }

    private fun updateNetworkState() {
        Log.d(TAG, "updateNetworkState llamado")
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val tm = telephonyManager ?: return
        
        val state = NetworkState()
        state.networkType = getNetworkTypeString(tm.dataNetworkType)
        state.networkGeneration = getNetworkGeneration(tm.dataNetworkType)
        state.operatorName = tm.networkOperatorName ?: "Desconocido"
        state.mccMnc = tm.networkOperator ?: ""
        state.isRoaming = tm.isNetworkRoaming

        val allCells = tm.allCellInfo
        if (allCells != null && allCells.isNotEmpty()) {
            state.cells = allCells.mapNotNull { parseCellInfo(it) }
            val primaryCell = allCells.firstOrNull { it.isRegistered }
            if (primaryCell != null) {
                state.primaryCell = parseCellInfo(primaryCell)
            }
        }

        currentNetworkState.set(state)
        networkStateListener?.invoke(state)
    }

    private fun parseCellInfo(cellInfo: android.telephony.CellInfo): CellInfo {
        val cell = CellInfo()
        cell.isRegistered = cellInfo.isRegistered

        when (cellInfo) {
            is android.telephony.CellInfoLte -> {
                cell.type = "LTE"
                cell.dbm = cellInfo.cellSignalStrength.dbm
                cell.cid = cellInfo.cellIdentity.ci.toString()
                cell.tac = cellInfo.cellIdentity.tac.toString()
                cell.pci = cellInfo.cellIdentity.pci.toString()
                cell.band = try {
                    val bands = cellInfo.cellIdentity.bands
                    if (bands != null && bands.isNotEmpty()) "B${bands[0]}" else "?"
                } catch (e: Exception) { "?" }
            }
            is android.telephony.CellInfoWcdma -> {
                cell.type = "WCDMA"
                cell.dbm = cellInfo.cellSignalStrength.dbm
                cell.cid = cellInfo.cellIdentity.cid?.toString() ?: "?"
                cell.lac = cellInfo.cellIdentity.lac?.toString() ?: "?"
                cell.band = "?"
            }
            is android.telephony.CellInfoGsm -> {
                cell.type = "GSM"
                cell.dbm = cellInfo.cellSignalStrength.dbm
                cell.cid = cellInfo.cellIdentity.cid.toString()
                cell.lac = cellInfo.cellIdentity.lac.toString()
                cell.band = "?"
                cell.bsic = cellInfo.cellIdentity.bsic.toString()
            }
            else -> {
                cell.type = "Otro"
                cell.dbm = -1
            }
        }

        return cell
    }

    private fun getNetworkTypeString(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            else -> "Desconocido"
        }
    }

    private fun getNetworkGeneration(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_GSM, TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
            else -> "?"
        }
    }

    private fun updateNotification() {
        val state = currentNetworkState.get()
        val primaryCell = state.primaryCell
        val info = "${state.operatorName} | ${state.networkGeneration} | ${primaryCell?.band ?: "?"} | ${primaryCell?.dbm ?: "?"} dBm"
        val notification = buildNotification(info)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        monitorHandler?.removeCallbacks(monitorRunnable ?: return)
        phoneStateListener?.let {
            telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE)
        }
        super.onDestroy()
    }
}