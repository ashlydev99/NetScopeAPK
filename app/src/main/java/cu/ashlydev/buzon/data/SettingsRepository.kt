package cu.ashlydev.buzon.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
    
    fun getWaitTime(): Int {
        return prefs.getInt("wait_time", 3)
    }
    
    fun saveWaitTime(value: Int) {
        prefs.edit().putInt("wait_time", value).apply()
    }
    
    fun getMessageTime(): Int {
        return prefs.getInt("message_time", 60)
    }
    
    fun saveMessageTime(value: Int) {
        prefs.edit().putInt("message_time", value).apply()
    }
    
    fun getGreetingPath(): String {
        return prefs.getString("greeting_path", "") ?: ""
    }
    
    fun saveGreetingPath(path: String) {
        prefs.edit().putString("greeting_path", path).apply()
    }
    
    fun getFarewellPath(): String {
        return prefs.getString("farewell_path", "") ?: ""
    }
    
    fun saveFarewellPath(path: String) {
        prefs.edit().putString("farewell_path", path).apply()
    }
}