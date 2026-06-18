package cu.ashlydev.buzon.utils

import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var filePath: String? = null
    
    fun startRecording(path: String) {
        try {
            filePath = path
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioSamplingRate(8000)
                setAudioEncodingBitRate(12200)
                setOutputFile(path)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback a MIC si VOICE_CALL no funciona
            try {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(path)
                    prepare()
                    start()
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }
    
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
    }
    
    fun getFilePath(): String? = filePath
    
    fun release() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
    }
}