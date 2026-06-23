package cu.ashlydev.home.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.ashlydev.home.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DeviceRepository
) : ViewModel() {
    
    fun deleteAllDevices() {
        viewModelScope.launch {
            repository.deleteAllDevices()
        }
    }
}