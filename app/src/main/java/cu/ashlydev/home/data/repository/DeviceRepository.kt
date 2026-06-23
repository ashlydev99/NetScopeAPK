package cu.ashlydev.home.data.repository

import cu.ashlydev.home.data.db.DeviceDao
import cu.ashlydev.home.data.db.DeviceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val deviceDao: DeviceDao
) {
    fun getAllDevices(): Flow<List<DeviceEntity>> = deviceDao.getAllDevices()
    
    suspend fun insertDevice(device: DeviceEntity): Long = deviceDao.insertDevice(device)
    
    suspend fun updateDevice(device: DeviceEntity) = deviceDao.updateDevice(device)
    
    suspend fun deleteDevice(device: DeviceEntity) = deviceDao.deleteDevice(device)
    
    suspend fun deleteAllDevices() = deviceDao.deleteAllDevices()
    
    suspend fun getDeviceByMac(mac: String): DeviceEntity? = deviceDao.getDeviceByMac(mac)
}