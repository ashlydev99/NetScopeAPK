package cu.ashlydev.home.ui.devicedetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cu.ashlydev.home.R
import cu.ashlydev.home.ui.theme.*

@Composable
fun DeviceDetailScreen(
    deviceId: Long,
    onBackClick: () -> Unit,
    onAdminClick: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel()
) {
    val device by viewModel.device.collectAsState()
    
    LaunchedEffect(deviceId) {
        viewModel.loadDevice(deviceId)
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
                    text = "Detalles del dispositivo:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            device?.let { dev ->
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Status
                    Text(
                        text = if (dev.isConnected) "Conectado" else "Desconectado",
                        fontSize = 14.sp,
                        color = if (dev.isConnected) ConnectedGreen else DisconnectedRed,
                        modifier = Modifier.align(Alignment.End)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Device name
                    Text(
                        text = dev.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkOnBackground
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Info card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            InfoRow("Modelo", dev.model ?: "Desconocido")
                            InfoRow("MAC", dev.macAddress)
                            InfoRow("Tipo", dev.type)
                            InfoRow("Conexión", dev.connectionType)
                            
                            if (dev.ipAddress != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("IP", color = DarkOnSurface)
                                    Row {
                                        Text(
                                            text = dev.ipAddress,
                                            color = BattleNetBlue
                                        )
                                        if (dev.connectionType == "WiFi") {
                                            Text(
                                                text = " Administrar",
                                                color = BattleNetBlue,
                                                textDecoration = TextDecoration.Underline,
                                                modifier = Modifier
                                                    .padding(start = 8.dp)
                                                    .clickable { onAdminClick() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.disconnectDevice() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DisconnectedRed
                            )
                        ) {
                            Text("Desconectar")
                        }
                        
                        Button(
                            onClick = { viewModel.connectDevice() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ConnectedGreen
                            )
                        ) {
                            Text("Conectar")
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BattleNetBlue)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = DarkOnSurface
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = DarkOnBackground
        )
    }
}