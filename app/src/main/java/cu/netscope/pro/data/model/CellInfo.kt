package cu.netscope.pro.data.model

data class CellInfo(
    var type: String? = "",
    var mcc: String? = "",
    var mnc: String? = "",
    var tac: String? = "",
    var cid: String? = "",
    var lac: String? = "",
    var pci: String? = "",
    var band: String? = "",
    var frequency: String? = "",
    var dbm: Int? = 0,
    var rsrp: Int? = 0,
    var rsrq: Int? = 0,
    var sinr: Int? = 0,
    var bsic: String? = "",
    var isRegistered: Boolean = false,
    var timingAdvance: Int? = 0,
    var estimatedDistance: Float? = 0f,
    var spectralEfficiency: Float? = 0f
) {
    val isConnected: Boolean get() = isRegistered
    
    val signalLevel: Int get() {
        val signal = dbm ?: 0
        return when {
            signal >= -90 -> 5  // Excelente
            signal >= -95 -> 4  // Buena
            signal >= -105 -> 3 // Regular
            signal >= -115 -> 2 // Mala
            signal >= -120 -> 1 // Muy débil
            signal > 0 -> 0     // Sin señal
            else -> 0
        }
    }
}