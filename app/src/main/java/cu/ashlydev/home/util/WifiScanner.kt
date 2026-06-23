package cu.ashlydev.home.util

import android.content.Context
import android.net.wifi.WifiManager
import android.net.Network
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class WifiDevice(
        val name: String,
        val ipAddress: String,
        val macAddress: String,
        val vendor: String = "Desconocido"
    )
    
    suspend fun scanNetwork(): List<WifiDevice> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<WifiDevice>()
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        try {
            val connectionInfo = wifiManager.connectionInfo
            val gatewayIp = getGatewayIp(connectionInfo)
            
            if (gatewayIp != null) {
                val subnet = gatewayIp.substringBeforeLast(".")
                
                // Escanear rango común (1-20)
                for (i in 1..20) {
                    val ip = "$subnet.$i"
                    try {
                        val inetAddress = InetAddress.getByName(ip)
                        if (inetAddress.isReachable(200)) {
                            devices.add(
                                WifiDevice(
                                    name = inetAddress.hostName ?: "Dispositivo WiFi",
                                    ipAddress = ip,
                                    macAddress = "Desconocida"
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Dispositivo no alcanzable
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        devices
    }
    
    @Suppress("DEPRECATION")
    private fun getGatewayIp(wifiInfo: WifiInfo): String? {
        return try {
            val dhcp = wifiInfo.dhcpInfo
            if (dhcp != null && dhcp.gateway != 0) {
                val gateway = dhcp.gateway
                String.format(
                    "%d.%d.%d.%d",
                    gateway and 0xff,
                    gateway shr 8 and 0xff,
                    gateway shr 16 and 0xff,
                    gateway shr 24 and 0xff
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}