package cu.ashlydev.home.ui.devicedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.ashlydev.home.data.db.DeviceEntity
import cu.ashlydev.home.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val repository: DeviceRepository
) : ViewModel() {
    
    private val _device = MutableStateFlow<DeviceEntity?>(null)
    val device: StateFlow<DeviceEntity?> = _device.asStateFlow()
    
    fun loadDevice(deviceId: Long) {
        viewModelScope.launch {
            repository.getAllDevices().collect { devices ->
                _device.value = devices.find { it.id == deviceId }
            }
        }
    }
    
    fun connectDevice() {
        viewModelScope.launch {
            _device.value?.let { device ->
                repository.updateDevice(device.copy(isConnected = true))
            }
        }
    }
    
    fun disconnectDevice() {
        viewModelScope.launch {
            _device.value?.let { device ->
                repository.updateDevice(device.copy(isConnected = false))
            }
        }
    }
}