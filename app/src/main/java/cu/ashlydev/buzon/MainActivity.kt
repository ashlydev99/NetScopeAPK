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
import cu.ashlydev.buzon.ui.screens.DetailScreen
import cu.ashlydev.buzon.ui.screens.MainScreen
import cu.ashlydev.buzon.ui.screens.SettingsScreen
import cu.ashlydev.buzon.ui.theme.BuzonVozTheme
import cu.ashlydev.buzon.ui.theme.DarkColorScheme

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val REQUEST_DIALER_ROLE = 1001
        private const val TAG = "MainActivity"
    }
    
    // Registro para solicitar permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "Todos los permisos concedidos")
            // Intentar solicitar rol de marcador después de los permisos
            requestDefaultDialerRole()
        } else {
            Log.w(TAG, "No se concedieron todos los permisos")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Solicitar permisos necesarios
        requestPermissions()
        
        // Manejar el intent de marcado (para compatibilidad con ACTION_DIAL)
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
        handleDialIntent(intent)
    }
    
    /**
     * Maneja el intent ACTION_DIAL para redirigir al marcador del sistema
     */
    private fun handleDialIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_DIAL) {
            Log.d(TAG, "Recibido ACTION_DIAL: ${intent.data}")
            // Redirigir a la app de marcado del sistema
            val defaultDialer = getSystemService(TelecomManager::class.java).defaultDialerPackage
            if (defaultDialer != null && defaultDialer != packageName) {
                val dialIntent = Intent(Intent.ACTION_DIAL, intent.data)
                startActivity(dialIntent)
                finish()
            }
        }
    }
    
    /**
     * Solicita los permisos necesarios para la app
     */
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
            // Si ya tiene todos los permisos, solicitar rol de marcador
            requestDefaultDialerRole()
        }
    }
    
    /**
     * Método alternativo: Solicita ser la app de teléfono predeterminada
     * usando RoleManager (Android 10+) o TelecomManager (Android 9-)
     */
    private fun requestDefaultDialerRole() {
        // Verificar que la app pueda contestar llamadas (tiene los permisos necesarios)
        if (!hasRequiredPermissions()) {
            Log.w(TAG, "No tiene todos los permisos, no se puede solicitar rol de marcador")
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ usa RoleManager
            try {
                val roleManager = getSystemService(android.app.role.RoleManager::class.java)
                if (roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_DIALER)) {
                    if (!roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_DIALER)) {
                        Log.d(TAG, "Solicitando rol de marcador con RoleManager")
                        val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_DIALER)
                        startActivityForResult(intent, REQUEST_DIALER_ROLE)
                    } else {
                        Log.d(TAG, "Ya tiene el rol de marcador")
                    }
                } else {
                    Log.w(TAG, "Rol de marcador no disponible en este dispositivo")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al solicitar rol de marcador: ${e.message}")
            }
        } else {
            // Android 9 y menor usa TelecomManager
            try {
                val telecomManager = getSystemService(TelecomManager::class.java)
                if (packageName != telecomManager.defaultDialerPackage) {
                    Log.d(TAG, "Solicitando ser marcador predeterminado con TelecomManager")
                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                        .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                    startActivityForResult(intent, REQUEST_DIALER_ROLE)
                } else {
                    Log.d(TAG, "Ya es el marcador predeterminado")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al solicitar ser marcador predeterminado: ${e.message}")
                // Fallback: abrir ajustes de marcador
                openDialerSettings()
            }
        }
    }
    
    /**
     * Verifica que todos los permisos necesarios estén concedidos
     */
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
    
    /**
     * Fallback: Abre los ajustes de aplicación de marcador predeterminada
     */
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
                Log.d(TAG, "✅ Rol de marcador concedido correctamente")
                android.widget.Toast.makeText(
                    this,
                    "App configurada como marcador predeterminado",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } else {
                Log.w(TAG, "❌ Rol de marcador no concedido o cancelado")
                android.widget.Toast.makeText(
                    this,
                    "Para que la app funcione, configúrala como marcador predeterminado en Ajustes",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}