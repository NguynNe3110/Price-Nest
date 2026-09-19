package com.uzuu.price_nest.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val PriceNestPrimary = Color(0xFF2E7D32)
val PriceNestOnPrimary = Color.White
val PriceNestSurface = Color(0xFFF5F5F5)
val PriceNestBackground = Color(0xFFFAFAFA)

private val LightColorScheme = lightColorScheme(
    primary = PriceNestPrimary,
    onPrimary = PriceNestOnPrimary,
    surface = PriceNestSurface,
    background = PriceNestBackground,
    secondary = Color(0xFFFF9800),
    tertiary = Color(0xFF4CAF50)
)

@Composable
fun PriceNestTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PriceNestPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}