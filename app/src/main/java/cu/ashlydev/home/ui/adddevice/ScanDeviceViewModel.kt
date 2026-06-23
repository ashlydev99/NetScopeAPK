package cu.ashlydev.home.ui.adddevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.ashlydev.home.data.db.DeviceEntity
import cu.ashlydev.home.data.repository.DeviceRepository
import cu.ashlydev.home.util.BluetoothScanner
import cu.ashlydev.home.util.WifiScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanDeviceViewModel @Inject constructor(
    private val wifiScanner: WifiScanner,
    private val bluetoothScanner: BluetoothScanner,
    private val repository: DeviceRepository
) : ViewModel() {
    
    private val _wifiDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val wifiDevices: StateFlow<List<ScannedDevice>> = _wifiDevices.asStateFlow()
    
    private val _bluetoothDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val bluetoothDevices: StateFlow<List<ScannedDevice>> = _bluetoothDevices.asStateFlow()
    
    fun startScanning() {
        viewModelScope.launch {
            // WiFi scan
            val wifiResults = wifiScanner.scanNetwork()
            _wifiDevices.value = wifiResults.map { device ->
                ScannedDevice(
                    name = device.name,
                    macAddress = device.macAddress,
                    ipAddress = device.ipAddress,
                    connectionType = "WiFi",
                    model = device.vendor
                )
            }
        }
        
        viewModelScope.launch {
            // Bluetooth scan
            val bluetoothList = mutableListOf<ScannedDevice>()
            bluetoothScanner.scanDevices().collect { device ->
                bluetoothList.add(
                    ScannedDevice(
                        name = device.name,
                        macAddress = device.macAddress,
                        connectionType = "Bluetooth"
                    )
                )
                _bluetoothDevices.value = bluetoothList.toList()
            }
        }
    }
    
    fun saveDevice(
        scannedDevice: ScannedDevice,
        customName: String,
        category: String,
        type: String
    ) {
        viewModelScope.launch {
            repository.insertDevice(
                DeviceEntity(
                    name = customName,
                    type = type,
                    category = category,
                    macAddress = scannedDevice.macAddress,
                    ipAddress = scannedDevice.ipAddress,
                    model = scannedDevice.model,
                    connectionType = scannedDevice.connectionType,
                    isConnected = true
                )
            )
        }
    }
}