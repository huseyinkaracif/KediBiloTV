package com.kedibilotv.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import com.kedibilotv.R
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.ui.common.ContentCard

@Composable
fun HomeTvContent(
    state: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String, Int) -> Unit,
    onContinueClick: (String, Int, Int?) -> Unit,
    onSettingsClick: () -> Unit
) {
    TvLazyColumn(
        modifier = Modifier.fillMaxSize().padding(start = 48.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (state.continueWatching.isNotEmpty()) {
            item {
                Text(stringResource(R.string.continue_watching), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.continueWatching) { item ->
                        ContentCard(
                            name = item.episodeTitle ?: item.name,
                            posterUrl = item.posterUrl,
                            onClick = { onContinueClick(item.type.name, item.streamId, item.episodeId) }
                        )
                    }
                }
            }
        }

        if (state.favorites.isNotEmpty()) {
            item {
                Text(stringResource(R.string.favorites), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.favorites) { item ->
                        ContentCard(
                            name = item.name,
                            posterUrl = item.posterUrl,
                            onClick = { onItemClick(item.type.name, item.streamId) }
                        )
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.categories), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    ContentCard(name = stringResource(R.string.live_tv), posterUrl = null, onClick = { onCategoryClick(ContentType.LIVE.name) })
                }
                item {
                    ContentCard(name = stringResource(R.string.movies), posterUrl = null, onClick = { onCategoryClick(ContentType.VOD.name) })
                }
                item {
                    ContentCard(name = stringResource(R.string.series), posterUrl = null, onClick = { onCategoryClick(ContentType.SERIES.name) })
                }
            }
        }
    }
}
