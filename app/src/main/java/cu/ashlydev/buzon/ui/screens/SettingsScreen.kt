package cu.ashlydev.buzon.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.ashlydev.buzon.data.SettingsRepository
import cu.ashlydev.buzon.ui.theme.ElectricBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }
    
    var waitTime by remember { mutableStateOf(settings.getWaitTime()) }
    var messageTime by remember { mutableStateOf(settings.getMessageTime()) }
    var greetingPath by remember { mutableStateOf(settings.getGreetingPath()) }
    var farewellPath by remember { mutableStateOf(settings.getFarewellPath()) }
    
    // Launchers para seleccionar archivos
    val greetingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val path = copyAudioToApp(context, it, "greeting")
            if (path != null) {
                greetingPath = path
                settings.saveGreetingPath(path)
            }
        }
    }
    
    val farewellLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val path = copyAudioToApp(context, it, "farewell")
            if (path != null) {
                farewellPath = path
                settings.saveFarewellPath(path)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = ElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A)
                )
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tiempo de espera
            SettingsCard(
                title = "Tiempo de espera",
                description = "Tiempo antes de contestar (segundos)"
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (waitTime > 1) {
                                waitTime--
                                settings.saveWaitTime(waitTime)
                            }
                        }
                    ) {
                        Text("-", color = ElectricBlue, fontSize = 24.sp)
                    }
                    
                    Text(
                        text = "$waitTime s",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.width(50.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = {
                            if (waitTime < 30) {
                                waitTime++
                                settings.saveWaitTime(waitTime)
                            }
                        }
                    ) {
                        Text("+", color = ElectricBlue, fontSize = 24.sp)
                    }
                }
            }
            
            // Tiempo del mensaje
            SettingsCard(
                title = "Tiempo del mensaje",
                description = "Segundos para grabar"
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (messageTime > 10) {
                                messageTime -= 5
                                settings.saveMessageTime(messageTime)
                            }
                        }
                    ) {
                        Text("-", color = ElectricBlue, fontSize = 24.sp)
                    }
                    
                    Text(
                        text = "$messageTime s",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.width(50.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = {
                            if (messageTime < 180) {
                                messageTime += 5
                                settings.saveMessageTime(messageTime)
                            }
                        }
                    ) {
                        Text("+", color = ElectricBlue, fontSize = 24.sp)
                    }
                }
            }
            
            // Selección de mensaje de saludo
            SettingsCard(
                title = "Mensaje de saludo",
                description = "Audio que escucha la otra persona"
            ) {
                Column {
                    Text(
                        text = if (greetingPath.isNotEmpty()) 
                            "✅ Audio seleccionado" 
                        else 
                            "⚠️ No seleccionado (usar predeterminado)",
                        color = if (greetingPath.isNotEmpty()) 
                            ElectricBlue 
                        else 
                            Color.Yellow.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            greetingLauncher.launch(arrayOf("audio/*", "audio/mpeg", "audio/wav"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Seleccionar audio", color = Color.White)
                    }
                }
            }
            
            // Selección de despedida
            SettingsCard(
                title = "Mensaje de despedida",
                description = "Audio que se reproduce al finalizar"
            ) {
                Column {
                    Text(
                        text = if (farewellPath.isNotEmpty()) 
                            "✅ Audio seleccionado" 
                        else 
                            "⚠️ No seleccionado (usar predeterminado)",
                        color = if (farewellPath.isNotEmpty()) 
                            ElectricBlue 
                        else 
                            Color.Yellow.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            farewellLauncher.launch(arrayOf("audio/*", "audio/mpeg", "audio/wav"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Seleccionar audio", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

private fun copyAudioToApp(context: Context, uri: Uri, prefix: String): String? {
    try {
        val contentResolver = context.contentResolver
        val fileName = "$prefix${System.currentTimeMillis()}.mp3"
        val destFile = File(context.filesDir, fileName)
        
        contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}