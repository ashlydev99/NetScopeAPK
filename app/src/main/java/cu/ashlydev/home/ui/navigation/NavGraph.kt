package cu.ashlydev.home.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cu.ashlydev.home.ui.adddevice.AlertReminderDialog
import cu.ashlydev.home.ui.adddevice.CategoryScreen
import cu.ashlydev.home.ui.adddevice.ScanDeviceScreen
import cu.ashlydev.home.ui.devicedetail.AdminWebViewScreen
import cu.ashlydev.home.ui.devicedetail.DeviceDetailScreen
import cu.ashlydev.home.ui.home.HomeScreen
import cu.ashlydev.home.ui.settings.SettingsScreen
import cu.ashlydev.home.ui.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ADD_DEVICE_ALERT = "add_device_alert"
    const val CATEGORY = "category"
    const val SCAN_DEVICE = "scan_device/{category}/{type}"
    const val DEVICE_DETAIL = "device_detail/{deviceId}"
    const val ADMIN_WEBVIEW = "admin_webview/{deviceId}"
    const val SETTINGS = "settings"
    
    fun scanDevice(category: String, type: String) = "scan_device/$category/$type"
    fun deviceDetail(deviceId: Long) = "device_detail/$deviceId"
    fun adminWebView(deviceId: Long) = "admin_webview/$deviceId"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.HOME) {
            HomeScreen(
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onAddDeviceClick = {
                    navController.navigate(Routes.ADD_DEVICE_ALERT)
                },
                onDeviceClick = { deviceId ->
                    navController.navigate(Routes.deviceDetail(deviceId))
                }
            )
        }
        
        composable(Routes.ADD_DEVICE_ALERT) {
            AlertReminderDialog(
                onAccept = {
                    navController.navigate(Routes.CATEGORY)
                },
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.CATEGORY) {
            CategoryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCategorySelected = { category, type ->
                    navController.navigate(Routes.scanDevice(category, type))
                }
            )
        }
        
        composable(
            Routes.SCAN_DEVICE,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: ""
            
            ScanDeviceScreen(
                category = category,
                type = type,
                onBackClick = {
                    navController.popBackStack()
                },
                onDeviceSaved = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            Routes.DEVICE_DETAIL,
            arguments = listOf(
                navArgument("deviceId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getLong("deviceId") ?: 0L
            
            DeviceDetailScreen(
                deviceId = deviceId,
                onBackClick = {
                    navController.popBackStack()
                },
                onAdminClick = {
                    navController.navigate(Routes.adminWebView(deviceId))
                }
            )
        }
        
        composable(
            Routes.ADMIN_WEBVIEW,
            arguments = listOf(
                navArgument("deviceId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getLong("deviceId") ?: 0L
            
            AdminWebViewScreen(
                deviceId = deviceId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}