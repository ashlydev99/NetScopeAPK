package cu.ashlydev.buzon.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

object FileHelper {
    
    fun copyAudioToApp(context: Context, uri: Uri, prefix: String): String? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "$prefix${System.currentTimeMillis()}.mp3"
            val destFile = File(context.filesDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun saveAudioToDevice(context: Context, filePath: String, phoneNumber: String): Boolean {
        return try {
            val sourceFile = File(filePath)
            if (!sourceFile.exists()) return false
            
            val fileName = "mensaje_${phoneNumber}_${System.currentTimeMillis()}.3gp"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ usa MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MUSICS}/BuzonVoz")
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(outputStream)
                        }
                    }
                }
                true
            } else {
                // Android 9 y menor
                val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSICS), "BuzonVoz")
                if (!musicDir.exists()) {
                    musicDir.mkdirs()
                }
                val destFile = File(musicDir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}