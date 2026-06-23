package cu.ashlydev.home.ui.adddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cu.ashlydev.home.R
import cu.ashlydev.home.ui.theme.*

@Composable
fun ScanDeviceScreen(
    category: String,
    type: String,
    onBackClick: () -> Unit,
    onDeviceSaved: () -> Unit,
    viewModel: ScanDeviceViewModel = hiltViewModel()
) {
    val wifiDevices by viewModel.wifiDevices.collectAsState()
    val bluetoothDevices by viewModel.bluetoothDevices.collectAsState()
    var selectedDevice by remember { mutableStateOf<ScannedDevice?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }
    
    if (showSaveDialog && selectedDevice != null) {
        SaveDeviceDialog(
            device = selectedDevice!!,
            onSave = { name ->
                viewModel.saveDevice(selectedDevice!!, name, category, type)
                showSaveDialog = false
                onDeviceSaved()
            },
            onCancel = {
                showSaveDialog = false
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Atrás",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Seleccionar dispositivo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // WiFi section
                item {
                    Text(
                        text = "Wi-Fi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BattleNetBlue,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (wifiDevices.isEmpty()) {
                    item {
                        Text(
                            text = "Buscando dispositivos WiFi...",
                            fontSize = 14.sp,
                            color = DarkOnSurface
                        )
                    }
                }
                
                items(wifiDevices) { device ->
                    ScannedDeviceCard(
                        device = device,
                        onClick = {
                            selectedDevice = device
                            showSaveDialog = true
                        }
                    )
                }
                
                // Bluetooth section
                item {
                    Text(
                        text = "Bluetooth",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BattleNetBlue,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (bluetoothDevices.isEmpty()) {
                    item {
                        Text(
                            text = "Buscando dispositivos Bluetooth...",
                            fontSize = 14.sp,
                            color = DarkOnSurface
                        )
                    }
                }
                
                items(bluetoothDevices) { device ->
                    ScannedDeviceCard(
                        device = device,
                        onClick = {
                            selectedDevice = device
                            showSaveDialog = true
                        }
                    )
                }
            }
        }
    }
}

data class ScannedDevice(
    val name: String,
    val macAddress: String,
    val ipAddress: String? = null,
    val connectionType: String, // "WiFi" o "Bluetooth"
    val model: String? = null
)

@Composable
fun ScannedDeviceCard(
    device: ScannedDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.disp),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    color = DarkOnBackground
                )
                Text(
                    text = device.macAddress,
                    fontSize = 12.sp,
                    color = DarkOnSurface
                )
                if (device.ipAddress != null) {
                    Text(
                        text = device.ipAddress,
                        fontSize = 12.sp,
                        color = BattleNetBlueLight
                    )
                }
            }
        }
    }
}