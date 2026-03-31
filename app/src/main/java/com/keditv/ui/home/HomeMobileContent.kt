package com.keditv.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.keditv.R
import com.keditv.domain.model.ContentItem
import com.keditv.domain.model.ContentType
import com.keditv.domain.model.Favorite
import com.keditv.domain.model.WatchHistory
import com.keditv.ui.common.ContentCard
import com.keditv.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeMobileContent(
    state: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String, Int) -> Unit,
    onContinueClick: (String, Int, Int?) -> Unit,
    onDeleteFromHistory: (WatchHistory) -> Unit,
    onSettingsClick: () -> Unit
) {
    // Staggered entrance state
    var tabsVisible by remember { mutableStateOf(false) }
    var sectionsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(250)
        tabsVisible = true
        delay(180)
        sectionsVisible = true
    }

    val tabsAlpha by animateFloatAsState(
        if (tabsVisible) 1f else 0f,
        tween(400, easing = FastOutSlowInEasing),
        label = "tabs_alpha"
    )
    val tabsSlide by animateFloatAsState(
        if (tabsVisible) 0f else 24f,
        tween(400, easing = FastOutSlowInEasing),
        label = "tabs_slide"
    )
    val sectionsAlpha by animateFloatAsState(
        if (sectionsVisible) 1f else 0f,
        tween(500, easing = FastOutSlowInEasing),
        label = "sections_alpha"
    )
    val sectionsSlide by animateFloatAsState(
        if (sectionsVisible) 0f else 20f,
        tween(500, easing = FastOutSlowInEasing),
        label = "sections_slide"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Hero banner
            if (state.featuredItems.isNotEmpty()) {
                item {
                    HeroBanner(
                        items = state.featuredItems,
                        currentIndex = state.currentFeaturedIndex,
                        onPlayClick = { item -> onItemClick(item.type.name, item.streamId) }
                    )
                }
            } else {
                item { Spacer(Modifier.height(80.dp)) }
            }

            // İçerik türü sekmeleri — slide in from below
            item {
                Box(
                    modifier = Modifier
                        .alpha(tabsAlpha)
                        .offset(y = tabsSlide.dp)
                ) {
                    ContentTypeTabs(onCategoryClick = onCategoryClick)
                }
            }

            // Devam Et
            if (state.continueWatching.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .alpha(sectionsAlpha)
                            .offset(y = sectionsSlide.dp)
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.continue_watching),
                            icon = Icons.Default.PlayArrow
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .alpha(sectionsAlpha)
                            .offset(y = sectionsSlide.dp)
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.continueWatching) { history ->
                                ContinueWatchingCard(
                                    history = history,
                                    onClick = {
                                        onContinueClick(history.type.name, history.streamId, history.episodeId)
                                    },
                                    onLongClick = { onDeleteFromHistory(history) }
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // Favoriler
            if (state.favorites.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .alpha(sectionsAlpha)
                            .offset(y = sectionsSlide.dp)
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.favorites),
                            icon = Icons.Default.Favorite,
                            iconTint = NeonFuchsia
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .alpha(sectionsAlpha)
                            .offset(y = sectionsSlide.dp)
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.favorites) { fav ->
                                FavoriteCard(
                                    name = fav.name,
                                    posterUrl = fav.posterUrl,
                                    onClick = { onItemClick(fav.type.name, fav.streamId) }
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }

        // Floating top bar — hero üzerine bindiriliyor
        FloatingTopBar(onSettingsClick = onSettingsClick)
    }
}

// ─────────────────────────────────────────────────────────────────
// Hero Banner — AnimatedContent crossfade between featured items
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(
    items: List<ContentItem>,
    currentIndex: Int,
    onPlayClick: (ContentItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        // Crossfading background + content
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                (fadeIn(tween(700)) + scaleIn(tween(700), initialScale = 1.04f)) togetherWith
                        fadeOut(tween(450))
            },
            modifier = Modifier.fillMaxSize(),
            label = "hero_content"
        ) { idx ->
            val featured = items[idx.coerceIn(0, items.lastIndex)]
            Box(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = featured.posterUrl,
                    contentDescription = featured.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Üst karartma — floating header okunabilirliği
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(NeonBackground.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                )

                // Alt karartma — içerik bilgisi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, NeonBackground.copy(alpha = 0.97f))
                            )
                        )
                )

                // İçerik bilgisi
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonCoral.copy(alpha = 0.15f),
                        modifier = Modifier.border(
                            1.dp, NeonCoral.copy(alpha = 0.5f), RoundedCornerShape(4.dp)
                        )
                    ) {
                        Text(
                            text = "ÖNE ÇIKAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCoral,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = featured.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!featured.rating.isNullOrBlank() && featured.rating != "0") {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "★ ${featured.rating}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { onPlayClick(featured) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCoral),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = NeonTextPrimary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.play),
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonTextPrimary
                        )
                    }
                }
            }
        }

        // Carousel nokta göstergeleri — AnimatedContent dışında, her zaman görünür
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEachIndexed { idx, _ ->
                    val isActive = idx == currentIndex
                    val dotWidth by animateFloatAsState(
                        targetValue = if (isActive) 20f else 6f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "dot_width_$idx"
                    )
                    val dotAlpha by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.4f,
                        animationSpec = tween(300),
                        label = "dot_alpha_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .width(dotWidth.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .alpha(dotAlpha)
                            .background(if (isActive) NeonCoral else NeonTextMuted)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// İçerik Türü Sekmeleri
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ContentTypeTabs(onCategoryClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ContentTypeTab(
            icon = Icons.Default.LiveTv,
            label = stringResource(R.string.live_tv),
            accentColor = NeonFuchsia,
            modifier = Modifier.weight(1f),
            onClick = { onCategoryClick(ContentType.LIVE.name) }
        )
        ContentTypeTab(
            icon = Icons.Default.Movie,
            label = stringResource(R.string.movies),
            accentColor = NeonCoral,
            modifier = Modifier.weight(1f),
            onClick = { onCategoryClick(ContentType.VOD.name) }
        )
        ContentTypeTab(
            icon = Icons.Default.VideoLibrary,
            label = stringResource(R.string.series),
            accentColor = NeonCyan,
            modifier = Modifier.weight(1f),
            onClick = { onCategoryClick(ContentType.SERIES.name) }
        )
    }
}

@Composable
private fun ContentTypeTab(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "tab_scale"
    )

    Box(
        modifier = modifier
            .height(72.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(NeonSurface)
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = NeonTextPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Devam Et Kartı (progress bar ile)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ContinueWatchingCard(history: WatchHistory, onClick: () -> Unit, onLongClick: () -> Unit) {
    val progress = if (history.durationMs > 0) {
        (history.positionMs.toFloat() / history.durationMs.toFloat()).coerceIn(0f, 1f)
    } else null

    val displayName = history.name.takeIf { it.isNotBlank() } ?: "İsimsiz"
    val subtitle = history.episodeTitle?.takeIf { it.isNotBlank() }

    var showDeleteDialog by remember { mutableStateOf(false) }

    ContentCard(
        name = displayName,
        posterUrl = history.posterUrl?.takeIf { it.isNotBlank() },
        progress = progress,
        subtitle = subtitle,
        onClick = onClick,
        onLongClick = { showDeleteDialog = true }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(stringResource(R.string.remove_from_history))
            },
            text = {
                Text(stringResource(R.string.remove_from_history_confirm))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onLongClick()
                    }
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Bölüm Başlığı
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, icon: ImageVector?, iconTint: Color = NeonCoral) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .background(NeonFuchsia, RoundedCornerShape(2.dp))
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = NeonTextPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Favori Kartı — kalp badge ile
// ─────────────────────────────────────────────────────────────────

@Composable
private fun FavoriteCard(name: String, posterUrl: String?, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "fav_scale"
    )

    Box(
        modifier = Modifier
            .width(130.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(NeonSurface)
            .border(
                1.dp,
                Brush.verticalGradient(listOf(NeonFuchsia.copy(alpha = 0.4f), NeonFuchsia.copy(alpha = 0f))),
                RoundedCornerShape(12.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxWidth().height(185.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_cat_placeholder),
                    error = painterResource(R.drawable.ic_cat_placeholder)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, NeonSurface.copy(alpha = 0.9f)))
                        )
                )
                // Kalp badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NeonFuchsia.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = NeonTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Floating Top Bar
// ─────────────────────────────────────────────────────────────────

@Composable
private fun FloatingTopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Kedi",
                style = MaterialTheme.typography.headlineSmall,
                color = NeonCoral
            )
            Text(
                text = "TV",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NeonSurface.copy(alpha = 0.8f))
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = NeonTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
