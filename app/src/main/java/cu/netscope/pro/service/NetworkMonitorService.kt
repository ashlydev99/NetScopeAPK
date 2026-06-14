package cu.netscope.pro.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import cu.netscope.pro.util.DistanceCalculator
import cu.netscope.pro.util.SpectralEfficiencyCalculator
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
    private val maxSpeedHistory = 30 // Últimos 30 segundos
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "network_monitor_channel"
        const val ACTION_STOP = "cu.netscope.pro.STOP_SERVICE"
        const val ACTION_UPDATE = "cu.netscope.pro.UPDATE_STATE"
        
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
        createNotificationChannel()
        setupTelephonyMonitoring()
        startSpeedMonitoring()
        startForeground(NOTIFICATION_ID, buildNotification("Iniciando..."))
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
            val rxSpeed = ((currentRxBytes - lastRxBytes) * 8f) / timeDelta // bits por segundo
            val txSpeed = ((currentTxBytes - lastTxBytes) * 8f) / timeDelta
            
            val speedMbps = (rxSpeed + txSpeed) / 1_000_000f
            
            synchronized(speedHistory) {
                speedHistory.add(speedMbps)
                if (speedHistory.size > maxSpeedHistory) {
                    speedHistory.removeAt(0)
                }
            }
            
            val state = currentNetworkState.get()
            state.copy(
                downloadSpeedBps = rxSpeed,
                uploadSpeedBps = txSpeed,
                speedHistory = speedHistory.toList()
            ).let { newState ->
                currentNetworkState.set(newState)
                networkStateListener?.invoke(newState)
            }
        }
        
        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
        lastUpdateTime = currentTime
        
        updateNotification()
    }
    
    private fun updateNetworkState() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return
        
        val tm = telephonyManager ?: return
        val state = NetworkState()
        
        // Información básica de red
        state.networkType = getNetworkTypeString(tm.dataNetworkType)
        state.networkGeneration = getNetworkGeneration(tm.dataNetworkType)
        state.operatorName = tm.networkOperatorName ?: "Desconocido"
        state.mccMnc = tm.networkOperator ?: ""
        state.isRoaming = tm.isNetworkRoaming
        
        // Información de celdas
        val allCells = tm.allCellInfo
        if (allCells != null && allCells.isNotEmpty()) {
            state.cells = allCells.mapNotNull { cellInfo ->
                parseCellInfo(cellInfo)
            }
            
            // Celda primaria (serving cell)
            val primaryCell = allCells.firstOrNull { it.isRegistered }
            if (primaryCell != null) {
                state.primaryCell = parseCellInfo(primaryCell)
            }
        }
        
        // Señal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val signalStrength = tm.signalStrength
            if (signalStrength != null) {
                state.dbm = signalStrength.dbm
                state.rsrp = signalStrength.dbm
                state.rsrq = signalStrength.getRsrq()
                state.sinr = signalStrength.getRssnr()
            }
        }
        
        currentNetworkState.set(state)
        networkStateListener?.invoke(state)
    }
    
    private fun parseCellInfo(cellInfo: android.telephony.CellInfo): CellInfo {
        val cell = CellInfo()
        
        when (cellInfo) {
            is android.telephony.CellInfoLte -> {
                val identity = cellInfo.cellIdentity
                cell.type = "LTE"
                cell.mcc = identity.mccString ?: ""
                cell.mnc = identity.mncString ?: ""
                cell.tac = identity.tac.toString()
                cell.cid = identity.ci.toString()
                cell.pci = identity.pci.toString()
                cell.band = identity.bands?.firstOrNull()?.toString() ?: "?"
                cell.frequency = identity.earfcn?.toString() ?: "?"
                cell.dbm = cellInfo.cellSignalStrength.dbm
                cell.rsrp = cellInfo.cellSignalStrength.dbm
                cell.rsrq = cellInfo.cellSignalStrength.rsrq
                cell.sinr = cellInfo.cellSignalStrength.rssi
                cell.isRegistered = cellInfo.isRegistered
                cell.timingAdvance = cellInfo.cellSignalStrength.timingAdvance
                cell.estimatedDistance = DistanceCalculator.estimateDistance(
                    cellInfo.cellSignalStrength.dbm,
                    identity.bands?.firstOrNull() ?: 3
                )
                cell.spectralEfficiency = SpectralEfficiencyCalculator.calculateLTE(
                    cellInfo.cellSignalStrength.dbm,
                    identity.bands?.firstOrNull() ?: 3
                )
            }
            is android.telephony.CellInfoWcdma -> {
                val identity = cellInfo.cellIdentity
                cell.type = "WCDMA"
                cell.mcc = identity.mccString ?: ""
                cell.mnc = identity.mncString ?: ""
                cell.cid = identity.cid?.toString() ?: "?"
                cell.lac = identity.lac?.toString() ?: "?"
                cell.band = "?"
                cell.dbm = cellInfo.cellSignalStrength.dbm
                cell.isRegistered = cellInfo.isRegistered
                cell.estimatedDistance = DistanceCalculator.estimateDistance(
                    cellInfo.cellSignalStrength.dbm, 1
                )
            }
            is android.telephony.CellInfoGsm -> {
                val identity = cellInfo.cellIdentity
                cell.type = "GSM"
                cell.mcc = identity.mccString ?: ""
                cell.mnc = identity.mncString ?: ""
                cell.cid = identity.cid.toString()
                cell.lac = identity.lac.toString()
                cell.band = "?"
                cell.dbm = cellInfo.cellSignalStrength.dbm
                cell.bsic = identity.bsic.toString()
                cell.isRegistered = cellInfo.isRegistered
                cell.estimatedDistance = DistanceCalculator.estimateDistance(
                    cellInfo.cellSignalStrength.dbm, 0
                )
            }
            is android.telephony.CellInfoNr -> {
                val identity = cellInfo.cellIdentity
                cell.type = "5G NR"
                cell.mcc = identity.mccString ?: ""
                cell.mnc = identity.mncString ?: ""
                cell.tac = identity.tac.toString()
                cell.cid = identity.nci?.toString() ?: "?"
                cell.pci = identity.pci.toString()
                cell.band = identity.bands?.firstOrNull()?.toString() ?: "?"
                cell.frequency = identity.nrarfcn?.toString() ?: "?"
                cell.dbm = cellInfo.cellSignalStrength.dbm
                cell.rsrp = cellInfo.cellSignalStrength.dbm
                cell.rsrq = cellInfo.cellSignalStrength.csiRsrq
                cell.sinr = cellInfo.cellSignalStrength.csiSinr
                cell.isRegistered = cellInfo.isRegistered
                cell.estimatedDistance = DistanceCalculator.estimateDistance(
                    cellInfo.cellSignalStrength.dbm,
                    identity.bands?.firstOrNull() ?: 78
                )
                cell.spectralEfficiency = SpectralEfficiencyCalculator.calculateNR(
                    cellInfo.cellSignalStrength.dbm,
                    identity.bands?.firstOrNull() ?: 78
                )
            }
        }
        
        return cell
    }
    
    private fun getNetworkTypeString(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO Rev. 0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO Rev. A"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO Rev. B"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
            else -> "Desconocido"
        }
    }
    
    private fun getNetworkGeneration(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT -> "2G"
            
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "3G"
            
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3.5G"
            
            TelephonyManager.NETWORK_TYPE_LTE,
            TelephonyManager.NETWORK_TYPE_EHRPD -> "4G"
            
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            
            else -> "?"
        }
    }
    
    private fun updateNotification() {
        val state = currentNetworkState.get()
        val primaryCell = state.primaryCell
        
        val info = buildString {
            append(state.operatorName)
            append(" | ")
            append(state.networkGeneration)
            append(" ")
            append(primaryCell?.band ?: "?")
            append(" | ")
            append(primaryCell?.dbm ?: "?")
            append(" dBm")
            if (primaryCell?.frequency != null && primaryCell.frequency != "?") {
                append(" | EARFCN: ")
                append(primaryCell.frequency)
            }
            if (primaryCell?.cid != null && primaryCell.cid != "?") {
                append(" | CID: ")
                append(primaryCell.cid)
            }
            if (primaryCell?.bsic != null && primaryCell.bsic != "?") {
                append(" | BSIC: ")
                append(primaryCell.bsic)
            }
        }
        
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