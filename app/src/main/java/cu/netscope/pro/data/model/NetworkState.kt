package cu.netscope.pro.data.model

data class NetworkState(
    var networkType: String = "Desconocido",
    var networkGeneration: String = "?",
    var operatorName: String = "Buscando...",
    var mccMnc: String = "",
    var isRoaming: Boolean = false,
    var dbm: Int = 0,
    var rsrp: Int = 0,
    var rsrq: Int = 0,
    var sinr: Int = 0,
    var downloadSpeedBps: Float = 0f,
    var uploadSpeedBps: Float = 0f,
    var speedHistory: List<Float> = emptyList(),
    var cells: List<CellInfo> = emptyList(),
    var primaryCell: CellInfo? = null
)