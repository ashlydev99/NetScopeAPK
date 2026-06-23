package cu.ashlydev.home.ui.devicedetail

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.ashlydev.home.data.db.DeviceEntity
import cu.ashlydev.home.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val repository: DeviceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _device = MutableStateFlow<DeviceEntity?>(null)
    val device: StateFlow<DeviceEntity?> = _device.asStateFlow()
    
    // Para rastrear si esta app inició el emparejamiento
    private var pairingInitiatedByApp = false
    private var bondStateReceiver: BroadcastReceiver? = null
    
    fun loadDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.getAllDevices().collect { devices ->
                devices.find { it.id == deviceId }?.let { dev ->
                    val isActuallyConnected = when (dev.connectionType) {
                        "WiFi" -> checkWifiConnection(dev)
                        "Bluetooth" -> checkBluetoothConnection(dev)
                        else -> false
                    }
                    _device.value = dev.copy(isConnected = isActuallyConnected)
                }
            }
        }
    }
    
    private fun checkWifiConnection(device: DeviceEntity): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val currentWifi = wifiManager.connectionInfo
            currentWifi?.ssid?.replace("\"", "") == device.ssid
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkBluetoothConnection(device: DeviceEntity): Boolean {
        return try {
            val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return false
            val bonded = adapter.bondedDevices.any { it.address == device.macAddress }
            // También verificar si hay conexión activa (no solo emparejado)
            val connected = try {
                val method = adapter.javaClass.getMethod("getConnectedDevices")
                @Suppress("UNCHECKED_CAST")
                val connectedDevices = method.invoke(adapter) as? Set<BluetoothDevice>
                connectedDevices?.any { it.address == device.macAddress } ?: false
            } catch (e: Exception) {
                bonded // Si no podemos verificar conexión activa, al menos verificar emparejado
            }
            connected || bonded
        } catch (e: SecurityException) {
            false
        }
    }
    
    fun connectDevice() {
        viewModelScope.launch {
            _device.value?.let { device ->
                when (device.connectionType) {
                    "WiFi" -> connectToWifi(device)
                    "Bluetooth" -> pairBluetooth(device)
                }
            }
        }
    }
    
    fun disconnectDevice() {
        viewModelScope.launch {
            _device.value?.let { device ->
                when (device.connectionType) {
                    "WiFi" -> disconnectFromWifi()
                    "Bluetooth" -> {
                        if (pairingInitiatedByApp) {
                            unpairBluetooth(device)
                        } else {
                            // Si no fue emparejado por la app, abrir ajustes
                            openBluetoothSettings()
                        }
                    }
                }
            }
        }
    }
    
    private fun connectToWifi(device: DeviceEntity) {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun disconnectFromWifi() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.disconnect()
            viewModelScope.launch {
                _device.value?.let { device ->
                    _device.value = device.copy(isConnected = false)
                    repository.updateDevice(device.copy(isConnected = false))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun pairBluetooth(device: DeviceEntity) {
        try {
            val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            
            if (adapter == null || !adapter.isEnabled) {
                openBluetoothSettings()
                return
            }
            
            val remoteDevice = adapter.getRemoteDevice(device.macAddress)
            
            // Quitar receiver anterior si existe
            unregisterBondReceiver()
            
            // Registrar receiver para seguimiento del emparejamiento
            bondStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                            val bondState = intent.getIntExtra(
                                BluetoothDevice.EXTRA_BOND_STATE, 
                                BluetoothDevice.BOND_NONE
                            )
                            viewModelScope.launch {
                                when (bondState) {
                                    BluetoothDevice.BOND_BONDED -> {
                                        pairingInitiatedByApp = true
                                        _device.value = _device.value?.copy(isConnected = true)
                                        _device.value?.let { dev -> repository.updateDevice(dev) }
                                    }
                                    BluetoothDevice.BOND_NONE -> {
                                        pairingInitiatedByApp = false
                                        _device.value = _device.value?.copy(isConnected = false)
                                        _device.value?.let { dev -> repository.updateDevice(dev) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(bondStateReceiver, filter)
            
            // Iniciar emparejamiento
            pairingInitiatedByApp = true
            remoteDevice.createBond()
            
        } catch (e: SecurityException) {
            openBluetoothSettings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun unpairBluetooth(device: DeviceEntity) {
        try {
            val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return
            
            val remoteDevice = adapter.getRemoteDevice(device.macAddress)
            
            // Usar reflexión para llamar a removeBond (método oculto)
            val method = remoteDevice.javaClass.getMethod("removeBond")
            val result = method.invoke(remoteDevice) as? Boolean
            
            if (result == true) {
                pairingInitiatedByApp = false
                viewModelScope.launch {
                    _device.value = _device.value?.copy(isConnected = false)
                    _device.value?.let { dev -> repository.updateDevice(dev) }
                }
            }
            
            // Limpiar receiver
            unregisterBondReceiver()
            
        } catch (e: SecurityException) {
            openBluetoothSettings()
        } catch (e: Exception) {
            // Si falla la reflexión, abrir ajustes
            openBluetoothSettings()
        }
    }
    
    private fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun unregisterBondReceiver() {
        try {
            bondStateReceiver?.let { context.unregisterReceiver(it) }
            bondStateReceiver = null
        } catch (e: Exception) {
            // Ya estaba desregistrado
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        unregisterBondReceiver()
    }
}