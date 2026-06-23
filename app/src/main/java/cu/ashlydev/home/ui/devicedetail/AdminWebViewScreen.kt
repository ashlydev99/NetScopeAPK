package cu.ashlydev.home.ui.devicedetail

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import cu.ashlydev.home.R
import cu.ashlydev.home.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdminWebViewScreen(
    deviceId: Long,
    onBackClick: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel()
) {
    val device by viewModel.device.collectAsState()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(deviceId) {
        viewModel.loadDevice(deviceId)
    }
    
    // Manejar botón atrás del sistema
    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Barra superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(DarkSurface)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Salir",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = device?.name ?: "Administrar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnBackground,
                    modifier = Modifier.weight(1f)
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.options),
                            contentDescription = "Opciones",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isDesktopMode) "Vista móvil" else "Vista escritorio",
                                    color = DarkOnBackground
                                )
                            },
                            onClick = {
                                isDesktopMode = !isDesktopMode
                                if (isDesktopMode) {
                                    webView?.settings?.userAgentString = 
                                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                } else {
                                    webView?.settings?.userAgentString = 
                                        "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36"
                                }
                                webView?.reload()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Actualizar página", color = DarkOnBackground)
                            },
                            onClick = {
                                webView?.reload()
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            // Progreso de carga
            var progress by remember { mutableIntStateOf(0) }
            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = BattleNetBlue
                )
            }
            
            // WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }
                        
                        // Persistencia de cookies
                        CookieManager.getInstance().apply {
                            setAcceptCookie(true)
                            setAcceptThirdPartyCookies(this@apply, true)
                        }
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                progress = 0
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                progress = 100
                            }
                        }
                        
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                        }
                        
                        webView = this
                    }
                },
                update = { view ->
                    device?.ipAddress?.let { ip ->
                        view.loadUrl("http://$ip")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}