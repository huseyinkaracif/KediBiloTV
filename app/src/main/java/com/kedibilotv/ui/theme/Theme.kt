package com.kedibilotv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KediColorScheme = darkColorScheme(
    primary = KediOrange,
    secondary = KediOrangeLight,
    background = KediBackground,
    surface = KediSurface,
    onPrimary = KediTextPrimary,
    onSecondary = KediBackground,
    onBackground = KediTextPrimary,
    onSurface = KediTextPrimary,
    error = KediError,
    onError = KediTextPrimary
)

@Composable
fun KediBiloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KediColorScheme,
        typography = KediTypography,
        content = content
    )
}
