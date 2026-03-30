package com.kedibilotv.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kedibilotv.ui.theme.NeonCyan
import com.kedibilotv.ui.theme.NeonTextSecondary

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "empty_float")

    // Gentle float up/down
    val floatY by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    // Subtle sway rotation
    val sway by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Paw blink — one paw dims and brightens slightly out of phase
    val pawBrightness by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "paw_blink"
    )

    // Fade-in on first appearance
    var visible by remember { mutableStateOf(false) }
    val entryAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "entry"
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier.fillMaxSize().alpha(entryAlpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Floating emoji group
            Box(
                modifier = Modifier.offset(y = floatY.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🐾",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .alpha(pawBrightness)
                            .rotate(-15f)
                            .offset(y = 4.dp)
                    )
                    Text(
                        text = "🐱",
                        fontSize = 56.sp,
                        modifier = Modifier.rotate(sway)
                    )
                    Text(
                        text = "🐾",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .alpha(1f - pawBrightness + 0.6f)
                            .rotate(15f)
                            .offset(y = 4.dp)
                    )
                }
            }

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = NeonTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Text(
                text = "— KediBilo",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan.copy(alpha = 0.6f)
            )
        }
    }
}
