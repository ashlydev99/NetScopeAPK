package cu.ashlydev.home.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
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
        
        try {
            val gatewayIp = getGatewayIp()
            
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
    
    private fun getGatewayIp(): String? {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return null
            val linkProperties = connectivityManager.getLinkProperties(network) ?: return null
            
            // Buscar la puerta de enlace en los LinkProperties
            linkProperties.routes?.firstOrNull { route ->
                route.gateway != null && !route.isDefaultRoute
            }?.gateway?.hostAddress ?: run {
                // Plan B: tomar el gateway de la ruta por defecto
                linkProperties.routes?.firstOrNull { route ->
                    route.isDefaultRoute && route.gateway != null
                }?.gateway?.hostAddress
            }
        } catch (e: Exception) {
            null
        }
    }
}