package cu.netscope.pro

import android.app.Application
import cu.netscope.pro.util.NotificationUtils

class NetScopeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createChannel(this)
    }
}