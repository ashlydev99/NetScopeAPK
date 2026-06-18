package cu.ashlydev.buzon

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cu.ashlydev.buzon.service.CallService
import cu.ashlydev.buzon.ui.screens.DetailScreen
import cu.ashlydev.buzon.ui.screens.MainScreen
import cu.ashlydev.buzon.ui.screens.SettingsScreen
import cu.ashlydev.buzon.ui.theme.BuzonVozTheme
import cu.ashlydev.buzon.ui.theme.DarkColorScheme
import cu.ashlydev.buzon.utils.NotificationHelper

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val REQUEST_DIALER_ROLE = 1001
        private const val TAG = "MainActivity"
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "Todos los permisos concedidos")
            requestDefaultDialerRole()
            startCallService()
        } else {
            Log.w(TAG, "No se concedieron todos los permisos")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Manejar cierre de la app desde notificación
        if (intent?.getBooleanExtra("exit", false) == true) {
            stopCallService()
            finish()
            return
        }
        
        requestPermissions()
        handleDialIntent(intent)
        
        setContent {
            BuzonVozTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkColorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(
                                onNavigateToDetail = { messageId ->
                                    navController.navigate("detail/$messageId")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onShowInfo = {
                                    // El diálogo de info se maneja dentro de MainScreen
                                }
                            )
                        }
                        composable("detail/{messageId}") { backStackEntry ->
                            val messageId = backStackEntry.arguments?.getString("messageId")?.toLong() ?: 0L
                            DetailScreen(
                                messageId = messageId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra("exit", false) == true) {
            stopCallService()
            finish()
            return
        }
        handleDialIntent(intent)
    }
    
    private fun handleDialIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_DIAL) {
            Log.d(TAG, "Recibido ACTION_DIAL: ${intent.data}")
            val defaultDialer = getSystemService(TelecomManager::class.java).defaultDialerPackage
            if (defaultDialer != null && defaultDialer != packageName) {
                val dialIntent = Intent(Intent.ACTION_DIAL, intent.data)
                startActivity(dialIntent)
                finish()
            }
        }
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
            }
        }
        
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            requestDefaultDialerRole()
            startCallService()
        }
    }
    
    private fun startCallService() {
        Log.d(TAG, "Iniciando CallService")
        val serviceIntent = Intent(this, CallService::class.java)
        startService(serviceIntent)
        
        // Mostrar notificación persistente
        NotificationHelper.showNotification(this)
    }
    
    private fun stopCallService() {
        Log.d(TAG, "Deteniendo CallService")
        val serviceIntent = Intent(this, CallService::class.java)
        stopService(serviceIntent)
        NotificationHelper.cancelNotification(this)
    }
    
    private fun requestDefaultDialerRole() {
        if (!hasRequiredPermissions()) {
            Log.w(TAG, "No tiene todos los permisos")
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = getSystemService(android.app.role.RoleManager::class.java)
                if (roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_DIALER)) {
                    if (!roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_DIALER)) {
                        Log.d(TAG, "Solicitando rol de marcador")
                        val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_DIALER)
                        startActivityForResult(intent, REQUEST_DIALER_ROLE)
                    } else {
                        Log.d(TAG, "Ya es el marcador predeterminado")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                openDialerSettings()
            }
        } else {
            try {
                val telecomManager = getSystemService(TelecomManager::class.java)
                if (packageName != telecomManager.defaultDialerPackage) {
                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                        .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                    startActivityForResult(intent, REQUEST_DIALER_ROLE)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                openDialerSettings()
            }
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS
        )
        
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val answerPermission = ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED
            return allGranted && answerPermission
        }
        
        return allGranted
    }
    
    private fun openDialerSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(android.net.Uri.parse("package:$packageName"))
                startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "No se pudieron abrir los ajustes: ${e2.message}")
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DIALER_ROLE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "✅ Rol de marcador concedido")
                startCallService()
            } else {
                Log.w(TAG, "❌ Rol de marcador no concedido")
                android.widget.Toast.makeText(
                    this,
                    "Configura la app como marcador predeterminado en Ajustes",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}