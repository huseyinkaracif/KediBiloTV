package com.kedibilotv.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kedibilotv.ui.theme.NeonCoral
import com.kedibilotv.ui.theme.NeonSurface
import com.kedibilotv.ui.theme.NeonTextPrimary

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    // One-shot horizontal shake on entry
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        shakeX.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 500
                0f    at 0
                (-14f) at 70
                14f   at 140
                (-10f) at 210
                10f   at 280
                (-5f) at 360
                0f    at 450
            }
        )
    }

    // Fade in on entry
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "error_alpha"
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier.fillMaxSize().alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "😿",
                fontSize = 52.sp,
                modifier = Modifier.offset(x = shakeX.value.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = NeonCoral
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = NeonSurface),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Tekrar Dene",
                    color = NeonTextPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
