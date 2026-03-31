package com.keditv.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.keditv.R
import com.keditv.ui.theme.NeonCoral
import com.keditv.ui.theme.NeonCyan
import com.keditv.ui.theme.NeonSurface
import com.keditv.ui.theme.NeonSurfaceRim
import com.keditv.ui.theme.NeonTextPrimary
import com.keditv.ui.theme.NeonTextSecondary

private val cardShape = RoundedCornerShape(12.dp)

@Composable
fun ContentCard(
    name: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )

    Box(
        modifier = modifier
            .width(140.dp)
            .scale(scale)
            .clip(cardShape)
            .background(NeonSurface)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(NeonCyan.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0f))
                ),
                shape = cardShape
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_cat_placeholder),
                    error = painterResource(R.drawable.ic_cat_placeholder)
                )
                // Alt gradient overlay — isim okunabilirliği için
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(NeonSurface.copy(alpha = 0f), NeonSurface.copy(alpha = 0.85f))
                            )
                        )
                )
                // İzleme ilerleme çubuğu
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = NeonCoral,
                        trackColor = NeonSurfaceRim
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonTextPrimary,
                    maxLines = if (subtitle != null) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
