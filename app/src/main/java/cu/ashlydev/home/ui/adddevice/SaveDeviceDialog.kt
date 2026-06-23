package cu.ashlydev.home.ui.adddevice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cu.ashlydev.home.ui.theme.*

@Composable
fun SaveDeviceDialog(
    device: ScannedDevice,
    category: String,
    type: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var deviceName by remember { mutableStateOf(device.name) }
    
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Guardar dispositivo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Nombre del dispositivo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BattleNetBlue,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedLabelColor = BattleNetBlue,
                        unfocusedLabelColor = DarkOnSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Device info
                DeviceInfoRow("MAC", device.macAddress)
                if (device.ipAddress != null) {
                    DeviceInfoRow("IP", device.ipAddress)
                }
                DeviceInfoRow("Tipo", device.connectionType)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkOnSurface
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = { onSave(deviceName) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BattleNetBlue
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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