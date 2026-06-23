package cu.ashlydev.home.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BattleNetBlue,
    onPrimary = DarkOnBackground,
    primaryContainer = BattleNetBlueDark,
    onPrimaryContainer = DarkOnBackground,
    secondary = BattleNetBlueLight,
    onSecondary = DarkOnBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurface,
    error = DisconnectedRed,
    onError = DarkOnBackground
)

@Composable
fun HomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}