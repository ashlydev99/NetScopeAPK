package cu.ashlydev.buzon.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import cu.ashlydev.buzon.MainActivity
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
    private var isGreetingPlayed = false
    private var isCallAnswered = false
    
    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        audioPlayer = AudioPlayer()
        audioRecorder = AudioRecorder()
        messageTime = settings.getMessageTime()
        Log.d("CallService", " Servicio creado")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CallService", "onStartCommand llamado")
        
        intent?.let {
            phoneNumber = it.getStringExtra("phone_number") ?: ""
            val callAnswered = it.getBooleanExtra("call_answered", false)
            Log.d("CallService", " Número: $phoneNumber, callAnswered: $callAnswered")
            
            if (callAnswered) {
                // La llamada ya fue contestada por InCallService
                Log.d("CallService", " Llamada ya contestada, reproduciendo saludo")
                serviceScope.launch {
                    delay(500)
                    playGreetingAndRecord()
                }
            } else if (phoneNumber.isNotEmpty()) {
                // Nueva llamada entrante
                Log.d("CallService", " Nueva llamada entrante, esperando...")
                startForeground(1001, createNotification())
            }
        }
        
        return START_STICKY
    }
    
    // Método llamado desde IncomingCallService cuando la llamada es contestada
    fun onCallAnswered(number: String) {
        Log.d("CallService", " Callback: Llamada contestada: $number")
        phoneNumber = number
        isCallAnswered = true
        
        serviceScope.launch {
            delay(500)
            if (!isGreetingPlayed) {
                playGreetingAndRecord()
            }
        }
    }
    
    private suspend fun playGreetingAndRecord() {
        try {
            Log.d("CallService", " Reproduciendo saludo para: $phoneNumber")
            
            // Reproducir saludo
            val greetingPath = settings.getGreetingPath()
            if (greetingPath.isNotEmpty() && java.io.File(greetingPath).exists()) {
                audioPlayer.play(greetingPath)
            } else {
                audioPlayer.playDefaultBeep()
            }
            
            isGreetingPlayed = true
            
            // Esperar a que termine el saludo
            var waitTime = 0
            while (audioPlayer.isPlaying() && waitTime < 10000) {
                delay(100)
                waitTime += 100
            }
            
            // Iniciar grabación
            startRecording()
            
        } catch (e: Exception) {
            Log.e("CallService", " Error: ${e.message}")
            startRecording()
        }
    }
    
    private suspend fun startRecording() {
        try {
            val fileName = "mensaje_${phoneNumber}_${System.currentTimeMillis()}.3gp"
            val file = java.io.File(filesDir, fileName)
            
            Log.d("CallService", " Iniciando grabación: ${file.absolutePath}")
            audioRecorder.startRecording(file.absolutePath)
            isRecording = true
            
            delay(messageTime * 1000L)
            
            // Reproducir despedida (si existe)
            val farewellPath = settings.getFarewellPath()
            if (farewellPath.isNotEmpty() && java.io.File(farewellPath).exists()) {
                audioPlayer.play(farewellPath)
                delay(2000)
            }
            
            stopRecording()
            
        } catch (e: Exception) {
            Log.e("CallService", " Error en grabación: ${e.message}")
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
                Log.d("CallService", " Mensaje guardado: $filePath")
                updateNotification()
            }
            
            hangUpCall()
        }
    }
    
    private fun hangUpCall() {
        try {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val method = tm.javaClass.getMethod("endCall")
            method.invoke(tm)
            Log.d("CallService", " Llamada colgada")
        } catch (e: Exception) {
            Log.e("CallService", " Error al colgar: ${e.message}")
        }
        stopForeground(true)
        stopSelf()
    }
    
    private fun createNotification(): Notification {
        val channelId = "buzon_voz_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Buzón de voz",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio de buzón de voz activo"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        
        val messageCount = MessageRepository.getAllMessages(this).size
        
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("exit", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val stopPendingIntent = PendingIntent.getActivity(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(" Buzón de voz activo")
            .setContentText("Mensajes: $messageCount")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, "Abrir", openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPendingIntent)
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
        Log.d("CallService", "Servicio destruido")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}