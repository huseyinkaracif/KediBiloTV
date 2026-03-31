package com.keditv.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keditv.ui.theme.NeonCoral
import com.keditv.ui.theme.NeonCyan
import com.keditv.ui.theme.NeonSurfaceRim
import kotlin.math.abs

private val loadingMessages = listOf(
    "Kanallar taranıyor",
    "Sunucu sorgulanıyor",
    "Playlist indiriliyor",
    "Kedi onayı bekleniyor",
    "İçerikler sıralanıyor",
    "Neon ışıklar yakılıyor",
    "Uzak sinema açılıyor"
)

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cat_loading")

    // Walking paw progress — 0→4 over 1600ms, each step = one paw
    val walkProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walk_progress"
    )

    // Cat breathing — subtle scale pulse
    val catScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cat_scale"
    )

    // Rotating message index every 2.2 seconds
    var msgIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2200)
            msgIndex = (msgIndex + 1) % loadingMessages.size
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Neon ring + cat center
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(96.dp),
                    color = NeonCoral,
                    strokeWidth = 3.dp,
                    trackColor = NeonSurfaceRim
                )
                Text(
                    text = "🐱",
                    fontSize = 42.sp,
                    modifier = Modifier.scale(catScale)
                )
            }

            // Walking paw prints
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PawPrint(alpha = pawAlpha(0, walkProgress), offsetY = (-6).dp, rotation = -18f)
                PawPrint(alpha = pawAlpha(1, walkProgress), offsetY = 6.dp,    rotation = 18f)
                PawPrint(alpha = pawAlpha(2, walkProgress), offsetY = (-6).dp, rotation = -18f)
                PawPrint(alpha = pawAlpha(3, walkProgress), offsetY = 6.dp,    rotation = 18f)
            }

            // Rotating loading message with crossfade
            AnimatedContent(
                targetState = loadingMessages[msgIndex],
                transitionSpec = {
                    fadeIn(tween(350)) togetherWith fadeOut(tween(250))
                },
                label = "loading_msg"
            ) { message ->
                Text(
                    text = "$message...",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan.copy(alpha = 0.75f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PawPrint(alpha: Float, offsetY: Dp, rotation: Float) {
    Text(
        text = "🐾",
        fontSize = 26.sp,
        modifier = Modifier
            .alpha(alpha)
            .offset(y = offsetY)
            .rotate(rotation)
    )
}

// Paw i brightens when walkProgress is near (i + 0.5), trails off after
private fun pawAlpha(pawIndex: Int, progress: Float): Float {
    val cycled = progress % 4f
    val center = pawIndex + 0.5f
    val dist = minOf(
        abs(cycled - center),
        abs(cycled - center + 4f),
        abs(cycled - center - 4f)
    )
    return when {
        dist < 0.35f -> 1f
        dist < 1.0f  -> 1f - ((dist - 0.35f) / 0.65f) * 0.82f
        else         -> 0.18f
    }
}
