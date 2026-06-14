package cu.netscope.pro

import android.app.Application

class NetScopeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: NetScopeApp
            private set
    }
}