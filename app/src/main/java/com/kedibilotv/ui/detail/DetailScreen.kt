package com.kedibilotv.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kedibilotv.R
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onPlay: (String, Int, Int?) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") } },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (state.isFavorite) stringResource(R.string.remove_favorite) else stringResource(R.string.add_favorite),
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AsyncImage(
                            model = state.posterUrl,
                            contentDescription = state.name,
                            modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    state.plot?.let { plot ->
                        item {
                            Text(plot, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                        }
                    }

                    if (state.contentType != ContentType.SERIES) {
                        item {
                            val hasProgress = state.watchHistory != null && state.watchHistory!!.positionMs > 0
                            Button(
                                onClick = { onPlay(state.contentType.name, state.streamId, null) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (hasProgress) stringResource(R.string.continue_play) else stringResource(R.string.play))
                            }
                        }
                    }

                    state.seriesInfo?.let { info ->
                        if (info.seasons.isNotEmpty()) {
                            item {
                                ScrollableTabRow(selectedTabIndex = state.selectedSeason - 1) {
                                    info.seasons.forEach { season ->
                                        Tab(
                                            selected = state.selectedSeason == season.seasonNumber,
                                            onClick = { viewModel.selectSeason(season.seasonNumber) },
                                            text = { Text("Sezon ${season.seasonNumber}") }
                                        )
                                    }
                                }
                            }

                            val currentSeason = info.seasons.find { it.seasonNumber == state.selectedSeason }
                            currentSeason?.episodes?.let { episodes ->
                                items(episodes) { episode ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onPlay(ContentType.SERIES.name, state.streamId, episode.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${episode.episodeNumber}",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(40.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(episode.title, style = MaterialTheme.typography.titleMedium)
                                                episode.duration?.let {
                                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
