package com.keditv.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.ui.PlayerView
import com.keditv.player.KediPlayer

data class TrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val name: String,
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val player = remember { KediPlayer.create(context) }

    var showTrackSheet by remember { mutableStateOf(false) }
    var audioTracks by remember { mutableStateOf<List<TrackInfo>>(emptyList()) }
    var subtitleTracks by remember { mutableStateOf<List<TrackInfo>>(emptyList()) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                audioTracks = buildTrackList(tracks, C.TRACK_TYPE_AUDIO)
                subtitleTracks = buildTrackList(tracks, C.TRACK_TYPE_TEXT)
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val pos = player.currentPosition
            val dur = player.duration
            if (dur > 0) viewModel.saveProgress(pos, dur)
            player.release()
        }
    }

    LaunchedEffect(state.streamUrl) {
        if (state.streamUrl.isNotEmpty()) {
            KediPlayer.play(player, state.streamUrl, state.startPositionMs)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    this.player = player
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = { showTrackSheet = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ClosedCaption,
                contentDescription = "Altyazı / Ses",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }

    if (showTrackSheet) {
        TrackSelectorSheet(
            audioTracks = audioTracks,
            subtitleTracks = subtitleTracks,
            onSelectAudio = { track ->
                val group = player.currentTracks.groups[track.groupIndex]
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex))
                    .build()
                showTrackSheet = false
            },
            onSelectSubtitle = { track ->
                if (track == null) {
                    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .build()
                } else {
                    val group = player.currentTracks.groups[track.groupIndex]
                    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex))
                        .build()
                }
                showTrackSheet = false
            },
            onDismiss = { showTrackSheet = false }
        )
    }
}

private fun buildTrackList(tracks: Tracks, trackType: Int): List<TrackInfo> {
    val result = mutableListOf<TrackInfo>()
    tracks.groups.forEachIndexed { groupIndex, group ->
        if (group.type == trackType) {
            for (trackIndex in 0 until group.length) {
                val format = group.getTrackFormat(trackIndex)
                val name = when {
                    !format.label.isNullOrBlank() -> format.label!!
                    !format.language.isNullOrBlank() -> format.language!!
                    else -> "Parça ${result.size + 1}"
                }
                result.add(
                    TrackInfo(
                        groupIndex = groupIndex,
                        trackIndex = trackIndex,
                        name = name,
                        isSelected = group.isTrackSelected(trackIndex)
                    )
                )
            }
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackSelectorSheet(
    audioTracks: List<TrackInfo>,
    subtitleTracks: List<TrackInfo>,
    onSelectAudio: (TrackInfo) -> Unit,
    onSelectSubtitle: (TrackInfo?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ses", "Altyazı")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A1A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0A1A1A),
                contentColor = Color(0xFF00FFD1)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) Color(0xFF00FFD1) else Color(0xFF7AA8A8)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> {
                    if (audioTracks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ses parçası bulunamadı", color = Color(0xFF7AA8A8))
                        }
                    } else {
                        LazyColumn {
                            itemsIndexed(audioTracks) { _, track ->
                                TrackItem(name = track.name, isSelected = track.isSelected) {
                                    onSelectAudio(track)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val subtitleDisabled = subtitleTracks.none { it.isSelected }
                    TrackItem(name = "Kapalı", isSelected = subtitleDisabled) {
                        onSelectSubtitle(null)
                    }
                    if (subtitleTracks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Altyazı bulunamadı", color = Color(0xFF7AA8A8))
                        }
                    } else {
                        LazyColumn {
                            itemsIndexed(subtitleTracks) { _, track ->
                                TrackItem(name = track.name, isSelected = track.isSelected) {
                                    onSelectSubtitle(track)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF00FFD1),
                unselectedColor = Color(0xFF7AA8A8)
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = name, color = Color(0xFFF0F8F8), style = MaterialTheme.typography.bodyLarge)
    }
}
