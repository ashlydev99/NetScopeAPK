package cu.netscope.pro.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PortScanner {
    
    data class ScanResult(
        val host: String,
        val port: Int,
        val isOpen: Boolean,
        val serviceName: String = guessService(port)
    )
    
    private val commonPorts = listOf(
        21, 22, 23, 25, 53, 80, 110, 135, 139, 143, 443, 445, 993, 995,
        1723, 3306, 3389, 5060, 5061, 5432, 5900, 6379, 8080, 8443, 9090
    )
    
    suspend fun scanHost(host: String, ports: List<Int> = commonPorts): List<ScanResult> {
        return withContext(Dispatchers.IO) {
            ports.map { port ->
                async {
                    val isOpen = try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(host, port), 1000)
                            true
                        }
                    } catch (e: Exception) {
                        false
                    }
                    ScanResult(host, port, isOpen)
                }
            }.awaitAll()
        }
    }
    
    suspend fun quickScan(): List<ScanResult> {
        return scanHost("127.0.0.1", listOf(53, 80, 443, 8080, 9090))
    }
    
    fun guessService(port: Int): String = when (port) {
        21 -> "FTP"
        22 -> "SSH"
        23 -> "Telnet"
        25 -> "SMTP"
        53 -> "DNS"
        80 -> "HTTP"
        110 -> "POP3"
        135 -> "RPC"
        139 -> "NetBIOS"
        143 -> "IMAP"
        443 -> "HTTPS"
        445 -> "SMB"
        993 -> "IMAPS"
        995 -> "POP3S"
        1723 -> "PPTP"
        3306 -> "MySQL"
        3389 -> "RDP"
        5060 -> "SIP"
        5061 -> "SIPS"
        5432 -> "PostgreSQL"
        5900 -> "VNC"
        6379 -> "Redis"
        8080 -> "HTTP-Alt"
        8443 -> "HTTPS-Alt"
        9090 -> "Web-Alt"
        else -> "?"
    }
}