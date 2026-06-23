package cu.ashlydev.home.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class BluetoothDeviceInfo(
        val name: String,
        val macAddress: String,
        val type: String = "Bluetooth"
    )
    
    fun scanDevices(): Flow<BluetoothDeviceInfo> = callbackFlow {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            close()
            return@callbackFlow
        }
        
        // Dispositivos emparejados
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        
        if (hasPermission) {
            bluetoothAdapter.bondedDevices.forEach { device ->
                trySend(
                    BluetoothDeviceInfo(
                        name = device.name ?: "Dispositivo Bluetooth",
                        macAddress = device.address ?: "Desconocida"
                    )
                )
            }
        }
        
        // Descubrimiento de nuevos dispositivos
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        device?.let {
                            if (hasPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                trySend(
                                    BluetoothDeviceInfo(
                                        name = it.name ?: "Dispositivo Bluetooth",
                                        macAddress = it.address ?: "Desconocida"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        
        context.registerReceiver(receiver, filter)
        
        if (hasPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            bluetoothAdapter.startDiscovery()
        }
        
        awaitClose {
            try {
                context.unregisterReceiver(receiver)
                if (hasPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    bluetoothAdapter.cancelDiscovery()
                }
            } catch (e: Exception) {
                // Receiver ya no registrado
            }
        }
    }
}