package cu.ashlydev.buzon.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cu.ashlydev.buzon.data.MessageRepository
import cu.ashlydev.buzon.data.models.VoiceMessage
import cu.ashlydev.buzon.ui.theme.ElectricBlue
import cu.ashlydev.buzon.utils.AudioPlayer
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    messageId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val message = remember { MessageRepository.getMessage(context, messageId) }
    
    if (message == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Mensaje no encontrado", color = Color.White)
        }
        return
    }
    
    var isPlaying by remember { mutableStateOf(false) }
    val audioPlayer = remember { AudioPlayer() }
    var currentPosition by remember { mutableStateOf(0) }
    val duration = remember { message.duration }
    
    // Reproducir al iniciar
    LaunchedEffect(Unit) {
        audioPlayer.setOnCompletionListener {
            isPlaying = false
            currentPosition = 0
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del mensaje",
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
                ),
                actions = {
                    IconButton(onClick = {
                        // Eliminar y volver
                        MessageRepository.deleteMessage(context, message.id)
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Información del mensaje
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "De: ${message.phoneNumber}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fecha: ${message.formattedDate}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Duración: ${message.duration} segundos",
                        color = ElectricBlue.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Controles de reproducción
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Barra de progreso
                    LinearProgressIndicator(
                        progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = ElectricBlue,
                        trackColor = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        
                        FloatingActionButton(
                            onClick = {
                                if (isPlaying) {
                                    audioPlayer.pause()
                                    isPlaying = false
                                } else {
                                    audioPlayer.play(message.filePath)
                                    isPlaying = true
                                    // Actualizar posición
                                    while (audioPlayer.isPlaying()) {
                                        currentPosition = audioPlayer.getCurrentPosition() / 1000
                                        delay(500)
                                    }
                                    isPlaying = false
                                    currentPosition = 0
                                }
                            },
                            containerColor = ElectricBlue,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Text(
                            text = formatTime(duration),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    saveAudioToDevice(context, message.filePath, message.phoneNumber)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = "Guardar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar en el dispositivo", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun delay(timeMillis: Long) {
    // Función auxiliar para delay en Composable
    // Se usa en el bucle de actualización de posición
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

private fun saveAudioToDevice(context: Context, filePath: String, phoneNumber: String) {
    try {
        val sourceFile = File(filePath)
        if (!sourceFile.exists()) return
        
        val fileName = "mensaje_${phoneNumber}_${System.currentTimeMillis()}.3gp"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ usa MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSICS)
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    FileOutputStream(outputStream).use { fos ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(fos)
                        }
                    }
                }
            }
        } else {
            // Android 9 y menor
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSICS)
            val destFile = File(musicDir, fileName)
            sourceFile.copyTo(destFile, overwrite = true)
        }
        
        // Mostrar mensaje de éxito (en producción usar Toast)
        android.widget.Toast.makeText(
            context,
            "Audio guardado correctamente",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Error al guardar: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}