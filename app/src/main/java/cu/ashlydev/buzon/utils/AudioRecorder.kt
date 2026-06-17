package cu.ashlydev.buzon.utils

import android.media.MediaRecorder
import java.io.File

class AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var filePath: String? = null
    
    fun startRecording(path: String) {
        try {
            filePath = path
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(path)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }
    
    fun getFilePath(): String? = filePath
    
    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
    }
}