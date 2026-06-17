package cu.ashlydev.buzon.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import cu.ashlydev.buzon.R
import cu.ashlydev.buzon.data.MessageRepository
import cu.ashlydev.buzon.data.SettingsRepository
import cu.ashlydev.buzon.data.models.VoiceMessage
import cu.ashlydev.buzon.utils.AudioPlayer
import cu.ashlydev.buzon.utils.AudioRecorder
import kotlinx.coroutines.*
import java.io.File

class CallService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var settings: SettingsRepository
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var audioRecorder: AudioRecorder
    private var isRecording = false
    private var phoneNumber: String = ""
    private var messageTime: Int = 60
    
    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        audioPlayer = AudioPlayer()
        audioRecorder = AudioRecorder()
        messageTime = settings.getMessageTime()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            phoneNumber = it.getStringExtra("phone_number") ?: ""
            val greetingPath = settings.getGreetingPath()
            val farewellPath = settings.getFarewellPath()
            
            // Iniciar servicio en foreground
            startForeground(1001, createNotification())
            
            // Esperar el tiempo configurado y contestar
            val waitTime = settings.getWaitTime() * 1000L
            serviceScope.launch {
                delay(waitTime)
                answerCall()
                playGreeting(greetingPath)
            }
        }
        return START_STICKY
    }
    
    private suspend fun answerCall() = withContext(Dispatchers.Main) {
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            val method = telephonyManager.javaClass.getMethod("answerRingingCall")
            method.invoke(telephonyManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun playGreeting(greetingPath: String) = withContext(Dispatchers.IO) {
        try {
            // Reproducir saludo
            if (greetingPath.isNotEmpty() && File(greetingPath).exists()) {
                audioPlayer.play(greetingPath)
            } else {
                // Saludo predeterminado (si existe)
                val defaultPath = "android.resource://${packageName}/${R.raw.default_greeting}"
                audioPlayer.play(defaultPath)
            }
            
            // Esperar a que termine el saludo
            while (audioPlayer.isPlaying()) {
                delay(100)
            }
            
            // Iniciar grabación
            startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla el saludo, empezar a grabar directamente
            startRecording()
        }
    }
    
    private suspend fun startRecording() = withContext(Dispatchers.IO) {
        try {
            val fileName = "mensaje_${phoneNumber}_${System.currentTimeMillis()}.3gp"
            val file = File(filesDir, fileName)
            
            audioRecorder.startRecording(file.absolutePath)
            isRecording = true
            
            // Grabar durante el tiempo configurado
            delay(messageTime * 1000L)
            
            // Reproducir despedida
            val farewellPath = settings.getFarewellPath()
            if (farewellPath.isNotEmpty() && File(farewellPath).exists()) {
                audioPlayer.play(farewellPath)
            }
            
            // Esperar a que termine la despedida
            delay(3000)
            
            // Detener grabación y guardar
            stopRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
        }
    }
    
    private fun stopRecording() {
        if (isRecording) {
            audioRecorder.stopRecording()
            isRecording = false
            
            // Guardar mensaje en el repositorio
            val duration = messageTime
            val filePath = audioRecorder.getFilePath()
            
            if (filePath != null && File(filePath).exists()) {
                val message = VoiceMessage(
                    id = System.currentTimeMillis(),
                    phoneNumber = phoneNumber,
                    filePath = filePath,
                    duration = duration,
                    timestamp = System.currentTimeMillis()
                )
                MessageRepository.saveMessage(this, message)
                
                // Actualizar notificación con el contador
                updateNotification()
            }
            
            // Colgar llamada
            hangUpCall()
            stopSelf()
        }
    }
    
    private fun hangUpCall() {
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            val method = telephonyManager.javaClass.getMethod("endCall")
            method.invoke(telephonyManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createNotification(): Notification {
        val channelId = "buzon_voz_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Buzón de voz",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación del servicio de buzón de voz"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        
        val messageCount = MessageRepository.getAllMessages(this).size
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Buzón de voz")
            .setContentText("Mensajes: $messageCount")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Salir",
                getExitPendingIntent()
            )
            .build()
    }
    
    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, createNotification())
    }
    
    private fun getExitPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("exit", true)
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        audioPlayer.release()
        audioRecorder.release()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}