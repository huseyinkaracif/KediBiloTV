package com.keditv.ui.detail

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.focus.onFocusChanged
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items as tvItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import com.keditv.R
import com.keditv.domain.model.ContentType
import com.keditv.ui.common.*
import com.keditv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    onPlay: (String, Int, Int?) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBackground)
    ) {
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            else -> DetailContent(
                state = state,
                onPlay = onPlay,
                onToggleFavorite = viewModel::toggleFavorite,
                onSelectSeason = viewModel::selectSeason
            )
        }

        // Floating back button
        if (!state.isLoading) {
            Box(modifier = Modifier.statusBarsPadding().padding(12.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonBackground.copy(alpha = 0.75f))
                        .border(1.dp, NeonSurfaceRim, CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = NeonTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState,
    onPlay: (String, Int, Int?) -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectSeason: (Int) -> Unit
) {
    val hasProgress = state.watchHistory != null && state.watchHistory.positionMs > 0
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { playFocusRequester.requestFocus() }
    }

    TvLazyColumn(modifier = Modifier.fillMaxSize()) {
        // ── Poster hero ──
        item {
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                AsyncImage(
                    model = state.posterUrl,
                    contentDescription = state.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Bottom gradient into background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, NeonBackground))
                        )
                )
                // Top gradient for back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(NeonBackground.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )
            }
        }

        // ── Title + rating + favorite ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonTextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!state.rating.isNullOrBlank() && state.rating != "0") {
                        Spacer(Modifier.height(6.dp))
                        RatingBadge(state.rating)
                    }
                }
                AnimatedFavoriteButton(
                    isFavorite = state.isFavorite,
                    onToggle = onToggleFavorite
                )
            }
        }

        // ── Plot ──
        state.plot?.takeIf { it.isNotBlank() }?.let { plot ->
            item {
                Text(
                    text = plot,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        // ── Play button (non-series) ──
        if (state.contentType != ContentType.SERIES) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onPlay(state.contentType.name, state.streamId, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp)
                        .focusRequester(playFocusRequester),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCoral)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = NeonTextPrimary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (hasProgress) stringResource(R.string.continue_play) else stringResource(R.string.play),
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonTextPrimary
                    )
                }

                if (hasProgress) {
                    val progress = (state.watchHistory!!.positionMs.toFloat() /
                            state.watchHistory.durationMs.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeonCoral,
                        trackColor = NeonSurfaceRim
                    )
                }
            }
        }

        // ── Series: season tabs + episodes ──
        state.seriesInfo?.let { info ->
            if (info.seasons.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ScrollableTabRow(
                        selectedTabIndex = (state.selectedSeason - 1).coerceAtLeast(0),
                        containerColor = NeonSurface,
                        contentColor = NeonCoral,
                        indicator = { tabPositions ->
                            val idx = (state.selectedSeason - 1).coerceIn(0, tabPositions.lastIndex)
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[idx]),
                                color = NeonCoral
                            )
                        }
                    ) {
                        info.seasons.forEach { season ->
                            Tab(
                                selected = state.selectedSeason == season.seasonNumber,
                                onClick = { onSelectSeason(season.seasonNumber) },
                                text = {
                                    Text(
                                        "Sezon ${season.seasonNumber}",
                                        color = if (state.selectedSeason == season.seasonNumber) NeonCoral else NeonTextSecondary
                                    )
                                }
                            )
                        }
                    }
                }

                val currentSeason = info.seasons.find { it.seasonNumber == state.selectedSeason }
                currentSeason?.episodes?.let { episodes ->
                    tvItems(episodes) { episode ->
                        EpisodeCard(
                            number = episode.episodeNumber,
                            title = episode.title,
                            duration = episode.duration,
                            onClick = { onPlay(ContentType.SERIES.name, state.streamId, episode.id) }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Suppress("UNUSED_PARAMETER")

// ─────────────────────────────────────────────────────────────────
// Animated Favorite Button — paw burst on favorite
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedFavoriteButton(isFavorite: Boolean, onToggle: () -> Unit) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    val heartScale = remember { Animatable(1f) }
    val burstAlpha = remember { Animatable(0f) }
    val burstY = remember { Animatable(0f) }
    var showBurst by remember { mutableStateOf(false) }

    LaunchedEffect(isFavorite) {
        if (isFavorite) {
            showBurst = true
            scope.launch {
                heartScale.animateTo(1.45f, tween(140, easing = FastOutSlowInEasing))
                heartScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh))
            }
            scope.launch {
                burstY.snapTo(0f)
                burstAlpha.snapTo(1f)
                burstY.animateTo(-44f, tween(500, easing = FastOutSlowInEasing))
                burstAlpha.animateTo(0f, tween(280))
                showBurst = false
                burstY.snapTo(0f)
            }
        } else {
            heartScale.animateTo(0.82f, tween(120))
            heartScale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showBurst) {
            Text(
                text = "🐾",
                fontSize = 18.sp,
                modifier = Modifier
                    .alpha(burstAlpha.value)
                    .offset(y = burstY.value.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(heartScale.value)
                .clip(CircleShape)
                .background(if (isFavorite) NeonFuchsia.copy(alpha = 0.15f) else NeonSurface)
                .border(1.dp, if (isFavorite) NeonFuchsia.copy(alpha = 0.55f) else NeonSurfaceRim, CircleShape)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) stringResource(R.string.remove_favorite) else stringResource(R.string.add_favorite),
                tint = if (isFavorite) NeonFuchsia else NeonTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Rating Badge
// ─────────────────────────────────────────────────────────────────

@Composable
private fun RatingBadge(rating: String) {
    val ratingFloat = rating.toFloatOrNull()?.coerceIn(0f, 10f) ?: return
    val filledStars = (ratingFloat / 2f).roundToInt().coerceIn(0, 5)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NeonCyan.copy(alpha = 0.1f))
            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            for (i in 1..5) {
                Text(
                    text = if (i <= filledStars) "★" else "☆",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = if (i <= filledStars) NeonCyan else NeonCyan.copy(alpha = 0.3f)
                )
            }
        }
        Text(
            text = String.format("%.1f", ratingFloat),
            style = MaterialTheme.typography.labelSmall,
            color = NeonTextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Episode Card
// ─────────────────────────────────────────────────────────────────

@Composable
private fun EpisodeCard(number: Int, title: String, duration: String?, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = when {
            isFocused -> 1.04f
            isPressed -> 0.97f
            else -> 1f
        },
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "ep_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) NeonSurfaceHigh else NeonSurface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) NeonCyan else NeonSurfaceRim,
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("$number", style = MaterialTheme.typography.titleLarge, color = NeonCoral, modifier = Modifier.width(36.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = NeonTextPrimary)
            if (duration != null) {
                Text(duration, style = MaterialTheme.typography.bodySmall, color = NeonTextMuted)
            }
        }
        Icon(Icons.Default.PlayArrow, null, tint = if (isFocused) NeonCyan else NeonTextMuted, modifier = Modifier.size(18.dp))
    }
}
