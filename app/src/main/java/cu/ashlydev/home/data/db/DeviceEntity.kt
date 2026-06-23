package cu.ashlydev.home.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val category: String,
    val macAddress: String,
    val ipAddress: String? = null,
    val model: String? = null,
    val connectionType: String, // "WiFi" o "Bluetooth"
    val isConnected: Boolean = false
)