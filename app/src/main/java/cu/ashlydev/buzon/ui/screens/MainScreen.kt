package cu.ashlydev.buzon.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cu.ashlydev.buzon.data.MessageRepository
import cu.ashlydev.buzon.data.models.VoiceMessage
import cu.ashlydev.buzon.ui.components.InfoDialog
import cu.ashlydev.buzon.ui.theme.ElectricBlue
import cu.ashlydev.buzon.ui.theme.ElectricBlueTransparent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowInfo: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showInfoDialog by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<VoiceMessage>>(emptyList()) }
    
    // Cargar mensajes al iniciar
    LaunchedEffect(Unit) {
        messages = MessageRepository.getAllMessages(context)
    }
    
    // Detectar cambios en mensajes (cuando se elimina uno)
    val refreshTrigger = remember { mutableStateOf(0) }
    LaunchedEffect(refreshTrigger.value) {
        messages = MessageRepository.getAllMessages(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Buzón de voz",
                        color = Color.White,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A)
                ),
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Información",
                            tint = ElectricBlue
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = ElectricBlue
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Voicemail,
                        contentDescription = "Sin mensajes",
                        tint = ElectricBlueTransparent,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay mensajes de voz",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Los mensajes aparecerán aquí",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        MessageItem(
                            message = message,
                            onClick = { onNavigateToDetail(message.id) },
                            onDelete = {
                                scope.launch {
                                    MessageRepository.deleteMessage(context, message.id)
                                    refreshTrigger.value++
                                }
                            }
                        )
                    }
                }
            }
            
            // Diálogo de información
            if (showInfoDialog) {
                InfoDialog(
                    onDismiss = { showInfoDialog = false }
                )
            }
        }
    }
}

@Composable
fun MessageItem(
    message: VoiceMessage,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message.phoneNumber,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = message.formattedDate,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${message.duration} segundos",
                    color = ElectricBlue.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}