package cu.ashlydev.buzon.data.models

data class VoiceMessage(
    val id: Long = 0,
    val phoneNumber: String,
    val filePath: String,
    val duration: Int,
    val timestamp: Long,
    val contactName: String? = null
) {
    val formattedDate: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }
}