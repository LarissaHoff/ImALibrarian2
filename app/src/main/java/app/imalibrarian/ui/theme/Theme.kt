package app.imalibrarian.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Cream,
    primaryContainer = TealLight,
    onPrimaryContainer = TextPrimary,
    secondary = Coral,
    onSecondary = Cream,
    secondaryContainer = Color(0xFFFFDDD5),
    onSecondaryContainer = Color(0xFF3D1C14),
    tertiary = MustardYellow,
    onTertiary = SpaceBlack,
    tertiaryContainer = Color(0xFFFFF0C0),
    onTertiaryContainer = Color(0xFF4A3D00),
    background = Cream,
    onBackground = TextPrimary,
    surface = OffWhite,
    onSurface = TextPrimary,
    surfaceVariant = WarmGray,
    onSurfaceVariant = TextSecondary,
    outline = TealDark,
    outlineVariant = Color(0xFFB0B0C0),
    error = Coral,
    onError = Cream,
    errorContainer = Color(0xFFFFDDD5),
    onErrorContainer = Color(0xFF3D1C14),
)

private val DarkColorScheme = darkColorScheme(
    primary = Turquoise,
    onPrimary = SpaceBlack,
    primaryContainer = TealDark,
    onPrimaryContainer = Turquoise,
    secondary = Coral,
    onSecondary = SpaceBlack,
    secondaryContainer = Color(0xFF8C3A2F),
    onSecondaryContainer = Color(0xFFFFDDD5),
    tertiary = MustardYellow,
    onTertiary = SpaceBlack,
    tertiaryContainer = Color(0xFF6B5D00),
    onTertiaryContainer = Color(0xFFFFF0C0),
    background = SpaceBlack,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = TealLight,
    outlineVariant = Color(0xFF404060),
    error = Color(0xFFFF8A80),
    onError = SpaceBlack,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun ImALibrarianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) SpaceBlack.toArgb() else Teal.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AtomicAgeTypography,
        content = content
    )
}