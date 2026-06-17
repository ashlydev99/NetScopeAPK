package cu.ashlydev.buzon.data

import android.content.Context
import android.content.SharedPreferences
import cu.ashlydev.buzon.data.models.VoiceMessage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object MessageRepository {
    private const val PREF_NAME = "messages_pref"
    private const val KEY_MESSAGES = "messages"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun getAllMessages(context: Context): List<VoiceMessage> {
        val json = getPrefs(context).getString(KEY_MESSAGES, "[]") ?: "[]"
        val array = JSONArray(json)
        val messages = mutableListOf<VoiceMessage>()
        
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            messages.add(
                VoiceMessage(
                    id = obj.getLong("id"),
                    phoneNumber = obj.getString("phoneNumber"),
                    filePath = obj.getString("filePath"),
                    duration = obj.getInt("duration"),
                    timestamp = obj.getLong("timestamp"),
                    contactName = if (obj.has("contactName")) obj.getString("contactName") else null
                )
            )
        }
        
        return messages.sortedByDescending { it.timestamp }
    }
    
    fun getMessage(context: Context, id: Long): VoiceMessage? {
        return getAllMessages(context).find { it.id == id }
    }
    
    fun saveMessage(context: Context, message: VoiceMessage) {
        val messages = getAllMessages(context).toMutableList()
        messages.add(message)
        saveMessages(context, messages)
    }
    
    fun deleteMessage(context: Context, id: Long) {
        val messages = getAllMessages(context).filter { it.id != id }
        saveMessages(context, messages)
        
        // Eliminar archivo de audio
        val message = getMessage(context, id)
        message?.let {
            val file = File(it.filePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }
    
    private fun saveMessages(context: Context, messages: List<VoiceMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            val obj = JSONObject()
            obj.put("id", message.id)
            obj.put("phoneNumber", message.phoneNumber)
            obj.put("filePath", message.filePath)
            obj.put("duration", message.duration)
            obj.put("timestamp", message.timestamp)
            message.contactName?.let { obj.put("contactName", it) }
            array.put(obj)
        }
        
        getPrefs(context).edit().putString(KEY_MESSAGES, array.toString()).apply()
    }
}