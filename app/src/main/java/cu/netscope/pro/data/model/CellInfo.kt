package cu.netscope.pro.data.model

data class CellInfo(
    var type: String = "",
    var mcc: String = "",
    var mnc: String = "",
    var tac: String = "",
    var cid: String = "",
    var lac: String = "",
    var pci: String = "",
    var band: String = "",
    var frequency: String = "",
    var dbm: Int = 0,
    var rsrp: Int = 0,
    var rsrq: Int = 0,
    var sinr: Int = 0,
    var bsic: String = "",
    var isRegistered: Boolean = false,
    var timingAdvance: Int = 0,
    var estimatedDistance: Float = 0f,
    var spectralEfficiency: Float = 0f
) {
    val isConnected: Boolean get() = isRegistered
    
    val distanceFormatted: String get() = when {
        estimatedDistance <= 0f -> "N/A"
        estimatedDistance < 1000f -> "${String.format("%.0f", estimatedDistance)} m"
        else -> "${String.format("%.2f", estimatedDistance / 1000f)} km"
    }
    
    val signalLevel: Int get() = when {
        dbm >= -75 -> 5 // Excelente
        dbm >= -85 -> 4 // Buena
        dbm >= -95 -> 3 // Regular
        dbm >= -105 -> 2 // Pobre
        dbm >= -115 -> 1 // Muy pobre
        else -> 0 // Sin señal
    }
}