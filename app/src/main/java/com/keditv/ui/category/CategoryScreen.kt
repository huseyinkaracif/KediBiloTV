package com.keditv.ui.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.keditv.R
import com.keditv.domain.model.Category
import com.keditv.domain.model.ContentItem
import com.keditv.domain.model.ContentType
import com.keditv.ui.common.EmptyState
import com.keditv.ui.common.ErrorState
import com.keditv.ui.common.LoadingIndicator
import com.keditv.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onNavigateToContent: (String, String) -> Unit,
    onNavigateToPlayer: (String, Int) -> Unit,
    onNavigateToDetail: (String, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val title = when (state.contentType) {
        ContentType.LIVE -> stringResource(R.string.live_tv)
        ContentType.VOD -> stringResource(R.string.movies)
        ContentType.SERIES -> stringResource(R.string.series)
    }

    LaunchedEffect(searchVisible) {
        if (searchVisible) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(visible = !searchVisible, enter = fadeIn(), exit = fadeOut()) {
                        Text(title, style = MaterialTheme.typography.headlineSmall, color = NeonTextPrimary)
                    }
                    AnimatedVisibility(visible = searchVisible, enter = fadeIn(), exit = fadeOut()) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::search,
                            placeholder = { Text(stringResource(R.string.search_hint), color = NeonTextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = NeonSurfaceHigh,
                                unfocusedContainerColor = NeonSurfaceHigh,
                                focusedTextColor = NeonTextPrimary,
                                unfocusedTextColor = NeonTextPrimary,
                                cursorColor = NeonCyan,
                                focusedIndicatorColor = NeonCyan,
                                unfocusedIndicatorColor = NeonSurfaceRim
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = NeonTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) viewModel.search("")
                    }) {
                        Icon(
                            if (searchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchVisible) stringResource(R.string.search_close) else stringResource(R.string.search),
                            tint = if (searchVisible) NeonCoral else NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBackground)
            )
        },
        containerColor = NeonBackground
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            // Arama modunda içerik sonuçları göster
            searchVisible && state.searchQuery.isNotBlank() -> {
                when {
                    state.isSearchingContent -> LoadingIndicator()
                    state.contentSearchResults.isEmpty() -> EmptyState(
                        "\"${state.searchQuery}\" ${stringResource(R.string.no_results)}"
                    )
                    else -> {
                        var gridVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { gridVisible = true }
                        val gridAlpha by animateFloatAsState(
                            targetValue = if (gridVisible) 1f else 0f,
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                            label = "grid_alpha"
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(140.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(gridAlpha)
                                .padding(padding)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.contentSearchResults) { item ->
                                ContentSearchCard(
                                    item = item,
                                    onClick = {
                                        if (item.type == ContentType.LIVE) {
                                            onNavigateToPlayer(item.type.name, item.streamId)
                                        } else {
                                            onNavigateToDetail(item.type.name, item.streamId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            // Normal kategori listesi
            state.filteredCategories.isEmpty() -> EmptyState(stringResource(R.string.no_content))
            else -> {
                var gridVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { gridVisible = true }
                val gridAlpha by animateFloatAsState(
                    targetValue = if (gridVisible) 1f else 0f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    label = "grid_alpha"
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(gridAlpha)
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredCategories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onNavigateToContent(state.contentType.name, category.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentSearchCard(item: ContentItem, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "search_card_scale"
    )

    Box(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(NeonSurface)
            .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.name,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_cat_placeholder),
                error = painterResource(R.drawable.ic_cat_placeholder)
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelMedium,
                color = NeonTextPrimary,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
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

    val hasImage = !category.imageUrl.isNullOrBlank()

    Box(
        modifier = Modifier
            .height(if (hasImage) 100.dp else 72.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(NeonSurface)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NeonCoral.copy(alpha = 0.4f), NeonSurfaceRim.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        if (hasImage) {
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_cat_placeholder)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonBackground.copy(alpha = 0.1f), NeonBackground.copy(alpha = 0.7f))
                        )
                    )
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                color = NeonTextPrimary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
