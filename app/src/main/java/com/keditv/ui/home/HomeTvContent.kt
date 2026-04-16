package com.keditv.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.keditv.domain.model.Favorite
import com.keditv.domain.model.WatchHistory
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import coil.compose.AsyncImage
import com.keditv.R
import com.keditv.domain.model.ContentItem
import com.keditv.domain.model.ContentType
import com.keditv.ui.common.ContentCard
import com.keditv.ui.theme.*

private sealed class ContextMenuTarget {
    data class History(val item: WatchHistory) : ContextMenuTarget()
    data class Fav(val item: Favorite) : ContextMenuTarget()
}

@Composable
fun HomeTvContent(
    state: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String, Int) -> Unit,
    onContinueClick: (String, Int, Int?) -> Unit,
    onSettingsClick: () -> Unit,
    onRemoveFromHistory: (WatchHistory) -> Unit = {},
    onRemoveFromFavorites: (Favorite) -> Unit = {}
) {
    var contextMenuTarget by remember { mutableStateOf<ContextMenuTarget?>(null) }

    // ─── Context menu dialog ───────────────────────────────────────
    contextMenuTarget?.let { target ->
        val itemName = when (target) {
            is ContextMenuTarget.History -> target.item.episodeTitle ?: target.item.name
            is ContextMenuTarget.Fav -> target.item.name
        }
        AlertDialog(
            onDismissRequest = { contextMenuTarget = null },
            title = { Text(itemName, style = MaterialTheme.typography.titleMedium, color = NeonTextPrimary, maxLines = 1) },
            text = null,
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        when (target) {
                            is ContextMenuTarget.History -> onItemClick(target.item.type.name, target.item.streamId)
                            is ContextMenuTarget.Fav -> onItemClick(target.item.type.name, target.item.streamId)
                        }
                        contextMenuTarget = null
                    }) { Text("Detaya Git", color = NeonCyan) }
                    TextButton(onClick = {
                        when (target) {
                            is ContextMenuTarget.History -> onRemoveFromHistory(target.item)
                            is ContextMenuTarget.Fav -> onRemoveFromFavorites(target.item)
                        }
                        contextMenuTarget = null
                    }) { Text("Kaldır", color = NeonCoral) }
                }
            },
            dismissButton = {
                TextButton(onClick = { contextMenuTarget = null }) { Text("İptal", color = NeonTextSecondary) }
            },
            containerColor = NeonSurface
        )
    }

    TvLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBackground),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item { TvHeader(onSettingsClick = onSettingsClick) }

        if (state.featuredItems.isNotEmpty()) {
            item {
                TvHeroBanner(
                    items = state.featuredItems,
                    currentIndex = state.currentFeaturedIndex,
                    onPlayClick = { item -> onItemClick(item.type.name, item.streamId) }
                )
            }
        }

        item { TvCategoryNav(onCategoryClick = onCategoryClick) }

        if (state.continueWatching.isNotEmpty()) {
            item {
                Text(
                    text = "Devam Et",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonTextPrimary,
                    modifier = Modifier.padding(start = 48.dp, top = 24.dp, bottom = 12.dp)
                )
            }
            item {
                TvLazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.continueWatching, key = { "${it.streamId}_${it.episodeId}" }) { item ->
                        ContentCard(
                            name = item.episodeTitle ?: item.name,
                            posterUrl = item.posterUrl,
                            onClick = { onContinueClick(item.type.name, item.streamId, item.episodeId) },
                            onLongClick = { contextMenuTarget = ContextMenuTarget.History(item) }
                        )
                    }
                }
            }
        }

        if (state.favorites.isNotEmpty()) {
            item {
                Text(
                    text = "Favoriler",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonTextPrimary,
                    modifier = Modifier.padding(start = 48.dp, top = 24.dp, bottom = 12.dp)
                )
            }
            item {
                TvLazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.favorites, key = { it.streamId }) { item ->
                        ContentCard(
                            name = item.name,
                            posterUrl = item.posterUrl,
                            onClick = { onItemClick(item.type.name, item.streamId) },
                            onLongClick = { contextMenuTarget = ContextMenuTarget.Fav(item) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Header: Logo + Settings ────────────────────────────────────

@Composable
private fun TvHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.app_icon_full),
            contentDescription = "KediTV",
            modifier = Modifier.height(44.dp).wrapContentWidth()
        )

        Spacer(Modifier.weight(1f))

        // Settings
        var settingsFocused by remember { mutableStateOf(false) }
        val settingsScale by animateFloatAsState(if (settingsFocused) 1.12f else 1f, label = "s_scale")
        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(settingsScale)
                .clip(CircleShape)
                .background(if (settingsFocused) NeonSurfaceHigh else NeonSurface)
                .border(2.dp, if (settingsFocused) NeonCyan else NeonSurfaceRim, CircleShape)
                .onFocusChanged { settingsFocused = it.isFocused }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onSettingsClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Ayarlar",
                tint = if (settingsFocused) NeonCyan else NeonTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─── Hero Banner ─────────────────────────────────────────────────

@Composable
private fun TvHeroBanner(
    items: List<ContentItem>,
    currentIndex: Int,
    onPlayClick: (ContentItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        // Background poster — crossfade
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(400)) },
            modifier = Modifier.fillMaxSize(),
            label = "tv_hero_bg"
        ) { idx ->
            AsyncImage(
                model = items[idx.coerceIn(0, items.lastIndex)].posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Sol gradient — içerik okunabilirliği
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.65f)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        0f to NeonBackground.copy(alpha = 0.97f),
                        0.7f to NeonBackground.copy(alpha = 0.7f),
                        1f to Color.Transparent
                    )
                )
        )

        // Alt gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, NeonBackground)))
        )

        // İçerik bilgisi — sol alt
        val featured = items[currentIndex.coerceIn(0, items.lastIndex)]
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 64.dp)
                .fillMaxWidth(0.48f)
        ) {
            Text(
                text = "ÖNE ÇIKAN",
                style = MaterialTheme.typography.labelLarge,
                color = NeonCoral
            )
            Spacer(Modifier.height(8.dp))
            AnimatedContent(
                targetState = featured.name,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
                label = "hero_title"
            ) { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.displaySmall,
                    color = NeonTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!featured.rating.isNullOrBlank() && featured.rating != "0") {
                Spacer(Modifier.height(6.dp))
                Text("★ ${featured.rating}", color = NeonCyan, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
            TvPlayButton(onClick = { onPlayClick(featured) })
        }

        // Nokta göstergeleri
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEachIndexed { idx, _ ->
                    val isActive = idx == currentIndex
                    val dotWidth by animateFloatAsState(
                        targetValue = if (isActive) 20f else 6f,
                        animationSpec = tween(300),
                        label = "dot_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .width(dotWidth.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .alpha(if (isActive) 1f else 0.4f)
                            .background(if (isActive) NeonCoral else NeonTextMuted)
                    )
                }
            }
        }
    }
}

@Composable
private fun TvPlayButton(onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1f, label = "play_scale")

    Row(
        modifier = Modifier
            .graphicsLayer { val s = scale; scaleX = s; scaleY = s }
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused) NeonCoralLight else NeonCoral)
            .border(2.dp, if (isFocused) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 32.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.PlayArrow, null, tint = NeonTextPrimary, modifier = Modifier.size(24.dp))
        Text("Oynat", style = MaterialTheme.typography.titleMedium, color = NeonTextPrimary)
    }
}

// ─── Kategori Navigasyonu ─────────────────────────────────────────

@Composable
private fun TvCategoryNav(onCategoryClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TvNavButton(
            icon = Icons.Default.LiveTv,
            label = "Canlı TV",
            accentColor = NeonFuchsia,
            modifier = Modifier.weight(1f),
            onClick = { onCategoryClick(ContentType.LIVE.name) }
        )
        TvNavButton(
            icon = Icons.Default.Movie,
            label = "Filmler",
            accentColor = NeonCoral,
            modifier = Modifier.weight(1f),
            onClick = { onCategoryClick(ContentType.VOD.name) }
        )
        TvNavButton(
            icon = Icons.Default.VideoLibrary,
            label = "Diziler",
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f),
            onClick = { onCategoryClick(ContentType.SERIES.name) }
        )
    }
}

@Composable
private fun TvNavButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.06f else 1f, label = "nav_scale")

    Column(
        modifier = modifier
            .height(90.dp)
            .graphicsLayer { val s = scale; scaleX = s; scaleY = s }
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) NeonSurfaceHigh else NeonSurface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) accentColor else NeonSurfaceRim,
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isFocused) accentColor else NeonTextSecondary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isFocused) accentColor else NeonTextPrimary
        )
    }
}

