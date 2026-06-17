package cu.ashlydev.buzon.utils

import android.media.MediaPlayer
import android.net.Uri
import java.io.File

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    
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
    
    fun play(uri: Uri) {
        try {
            release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(uri.toString())
                prepare()
                start()
            }
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
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
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