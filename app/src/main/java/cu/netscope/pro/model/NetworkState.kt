package cu.netscope.pro.model

data class NetworkState(
    val operator: String?,
    val networkType: String?,
    val generation: String?,
    val cells: List<CellInfoModel>?,
    val primaryCell: CellInfoModel?,
    val downloadKbps: Long?,
    val uploadKbps: Long?
)