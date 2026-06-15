package cu.netscope.pro.model

data class CellInfoModel(
    val type: String?,
    val band: String?,
    val dbm: Int?,
    val cid: Long?,
    val lac: Int?,
    val tac: Int?,
    val pci: Int?,
    val bsic: Int?,
    val frequency: Int?,
    val isRegistered: Boolean?
)