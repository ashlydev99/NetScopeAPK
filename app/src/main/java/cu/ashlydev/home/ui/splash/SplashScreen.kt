package cu.ashlydev.home.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import cu.ashlydev.home.R
import cu.ashlydev.home.ui.theme.DarkBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }
    
    Image(
        painter = painterResource(id = R.drawable.splash),
        contentDescription = "Splash",
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentScale = ContentScale.FillBounds
    )
}