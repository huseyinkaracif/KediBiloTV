package com.kedibilotv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonGatosColorScheme = darkColorScheme(
    primary          = NeonCoral,
    onPrimary        = NeonTextPrimary,
    primaryContainer = NeonCoralLight,
    onPrimaryContainer = NeonBackground,

    secondary        = NeonFuchsia,
    onSecondary      = NeonTextPrimary,
    secondaryContainer = NeonFuchsiaDim,
    onSecondaryContainer = NeonTextPrimary,

    tertiary         = NeonCyan,
    onTertiary       = NeonBackground,
    tertiaryContainer = NeonCyanDim,
    onTertiaryContainer = NeonBackground,

    background       = NeonBackground,
    onBackground     = NeonTextPrimary,

    surface          = NeonSurface,
    onSurface        = NeonTextPrimary,
    surfaceVariant   = NeonSurfaceHigh,
    onSurfaceVariant = NeonTextSecondary,

    outline          = NeonSurfaceRim,
    outlineVariant   = NeonSurfaceHigh,

    error            = NeonError,
    onError          = NeonTextPrimary,
)

@Composable
fun KediTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeonGatosColorScheme,
        typography  = KediTypography,
        content     = content
    )
}
