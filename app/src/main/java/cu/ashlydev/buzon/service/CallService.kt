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
        
        // Crear canal de notificación para Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "buzon_voz_channel",
                "Buzón de voz",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio de buzón de voz"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            phoneNumber = it.getStringExtra("phone_number") ?: ""
            
            // Iniciar servicio en foreground
            startForeground(1001, createNotification())
            
            // Esperar el tiempo configurado y contestar
            val waitTime = settings.getWaitTime() * 1000L
            serviceScope.launch {
                delay(waitTime)
                answerCall()
                playGreeting()
            }
        }
        return START_STICKY
    }
    
    private suspend fun answerCall() {
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            // Método alternativo para Android 10
            val method = telephonyManager.javaClass.getMethod("answerRingingCall")
            method.invoke(telephonyManager)
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla, intentar con InCallService
            try {
                val intent = Intent(this, IncomingCallService::class.java)
                startService(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }
    
    private suspend fun playGreeting() {
        try {
            val greetingPath = settings.getGreetingPath()
            
            // Reproducir saludo
            if (greetingPath.isNotEmpty() && java.io.File(greetingPath).exists()) {
                audioPlayer.play(greetingPath)
            } else {
                // Usar un tono simple generado programáticamente
                audioPlayer.playDefaultBeep()
            }
            
            // Esperar a que termine el saludo (máximo 10 segundos)
            var waitTime = 0
            while (audioPlayer.isPlaying() && waitTime < 10000) {
                delay(100)
                waitTime += 100
            }
            
            // Iniciar grabación
            startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            startRecording()
        }
    }
    
    private suspend fun startRecording() {
        try {
            val fileName = "mensaje_${phoneNumber}_${System.currentTimeMillis()}.3gp"
            val file = java.io.File(filesDir, fileName)
            
            audioRecorder.startRecording(file.absolutePath)
            isRecording = true
            
            // Grabar durante el tiempo configurado
            delay(messageTime * 1000L)
            
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
            
            val filePath = audioRecorder.getFilePath()
            
            if (filePath != null && java.io.File(filePath).exists()) {
                val message = VoiceMessage(
                    id = System.currentTimeMillis(),
                    phoneNumber = phoneNumber,
                    filePath = filePath,
                    duration = messageTime,
                    timestamp = System.currentTimeMillis()
                )
                MessageRepository.saveMessage(this, message)
                updateNotification()
            }
            
            // Colgar llamada
            hangUpCall()
            stopForeground(true)
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
        val messageCount = MessageRepository.getAllMessages(this).size
        
        return NotificationCompat.Builder(this, "buzon_voz_channel")
            .setContentTitle("Buzón de voz")
            .setContentText("Mensajes: $messageCount")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, createNotification())
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        audioPlayer.release()
        audioRecorder.release()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}