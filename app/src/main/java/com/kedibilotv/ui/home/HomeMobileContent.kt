package com.kedibilotv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kedibilotv.R
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.ui.common.ContentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMobileContent(
    state: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String, Int) -> Unit,
    onContinueClick: (String, Int, Int?) -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KediBiloTV", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (state.featuredItems.isNotEmpty()) {
                item {
                    val featured = state.featuredItems[state.currentFeaturedIndex]
                    AsyncImage(
                        model = featured.posterUrl,
                        contentDescription = featured.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onItemClick(featured.type.name, featured.streamId) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (state.continueWatching.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.continue_watching))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                    SectionHeader(stringResource(R.string.favorites))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                SectionHeader("Kategoriler")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryButton(stringResource(R.string.live_tv), Modifier.weight(1f)) { onCategoryClick(ContentType.LIVE.name) }
                    CategoryButton(stringResource(R.string.movies), Modifier.weight(1f)) { onCategoryClick(ContentType.VOD.name) }
                    CategoryButton(stringResource(R.string.series), Modifier.weight(1f)) { onCategoryClick(ContentType.SERIES.name) }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun CategoryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
