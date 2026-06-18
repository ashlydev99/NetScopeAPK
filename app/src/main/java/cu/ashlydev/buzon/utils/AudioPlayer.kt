package cu.ashlydev.buzon.utils

import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import java.io.File

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    fun play(filePath: String) {
        try {
            release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun playDefaultBeep() {
        try {
            toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_VOICE_CALL, 100)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
            
            // Detener después de 2 segundos
            mainHandler.postDelayed({
                toneGenerator?.stopTone()
                toneGenerator?.release()
                toneGenerator = null
            }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun pause() {
        mediaPlayer?.pause()
    }
    
    fun resume() {
        mediaPlayer?.start()
    }
    
    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        toneGenerator?.stopTone()
        toneGenerator?.release()
        toneGenerator = null
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        toneGenerator?.release()
        toneGenerator = null
    }
    
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            listener()
        }
    }
}