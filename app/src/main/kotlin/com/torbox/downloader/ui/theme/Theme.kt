package com.torbox.downloader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val lightScheme = lightColorScheme(
    primary = Color(0xFF6750a4),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFFffffff),
    onPrimaryContainer = Color(0xFF21005e),
    secondary = Color(0xFF625b71),
    onSecondary = Color(0xFFffffff),
    secondaryContainer = Color(0xFFe8def8),
    onSecondaryContainer = Color(0xFF1e192b),
    tertiary = Color(0xFF7d5260),
    onTertiary = Color(0xFFffffff),
    tertiaryContainer = Color(0xFFffd8e4),
    onTertiaryContainer = Color(0xFF31111d),
    error = Color(0xFFb3261e),
    onError = Color(0xFFffffff),
    errorContainer = Color(0xFFf9dedc),
    onErrorContainer = Color(0xFF410e0b),
    background = Color(0xFFfffbfe),
    onBackground = Color(0xFF1c1b1f),
    surface = Color(0xFFfffbfe),
    onSurface = Color(0xFF1c1b1f),
    surfaceVariant = Color(0xFFffffff),
    onSurfaceVariant = Color(0xFF79747e),
    outline = Color(0xFF79747e),
    outlineVariant = Color(0xFFcac7d0),
)

private val darkScheme = darkColorScheme(
    primary = Color(0xFFd0bcff),
    onPrimary = Color(0xFF371e55),
    primaryContainer = Color(0xFF4f378b),
    onPrimaryContainer = Color(0xFFeaddff),
    secondary = Color(0xFFccc7d8),
    onSecondary = Color(0xFF332d41),
    secondaryContainer = Color(0xFF4a4458),
    onSecondaryContainer = Color(0xFFe8def8),
    tertiary = Color(0xFFf8b4d0),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633b48),
    onTertiaryContainer = Color(0xFFffd8e4),
    error = Color(0xFFf9dedc),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8c1d18),
    onErrorContainer = Color(0xFFf9dedc),
    background = Color(0xFF1c1b1f),
    onBackground = Color(0xFFe6e1e6),
    surface = Color(0xFF1c1b1f),
    onSurface = Color(0xFFe6e1e6),
    surfaceVariant = Color(0xFF49454e),
    onSurfaceVariant = Color(0xFF938f99),
    outline = Color(0xFF938f99),
    outlineVariant = Color(0xFF49454e),
)

@Composable
fun TorBoxDownloaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
