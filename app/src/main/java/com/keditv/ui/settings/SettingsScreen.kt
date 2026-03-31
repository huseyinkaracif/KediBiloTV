package com.keditv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.keditv.R
import com.keditv.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), color = NeonTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = NeonTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBackground)
            )
        },
        containerColor = NeonBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "KediTV v1.0.0",
                style = MaterialTheme.typography.titleMedium,
                color = NeonTextSecondary
            )

            HorizontalDivider(color = NeonSurfaceRim)

            Text(
                text = stringResource(R.string.buffer_size),
                style = MaterialTheme.typography.titleMedium,
                color = NeonTextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BufferSize.entries.forEach { size ->
                    val selected = state.bufferSize == size
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setBufferSize(size) },
                        label = { Text(size.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCoral.copy(alpha = 0.15f),
                            selectedLabelColor = NeonCoral,
                            containerColor = NeonSurface,
                            labelColor = NeonTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = NeonSurfaceRim,
                            selectedBorderColor = NeonCoral.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            HorizontalDivider(color = NeonSurfaceRim)

            Button(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonError.copy(alpha = 0.12f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    color = NeonError
                )
            }

            HorizontalDivider(color = NeonSurfaceRim)

            AboutSection()
        }
    }
}

@Composable
private fun AboutSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeonSurface)
            .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Hakkımızda",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "KediTV",
                style = MaterialTheme.typography.headlineSmall,
                color = NeonCoral,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Neon Gatos Cinema",
                style = MaterialTheme.typography.labelMedium,
                color = NeonTextSecondary
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = NeonSurfaceRim.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Geliştirici",
                style = MaterialTheme.typography.labelSmall,
                color = NeonTextMuted
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeveloperChip("Üçin")
                Text("&", style = MaterialTheme.typography.bodySmall, color = NeonTextMuted)
                DeveloperChip("Ceynep")
            }
        }
    }
}

@Composable
private fun DeveloperChip(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NeonBackground)
            .border(1.dp, NeonFuchsia.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = NeonFuchsia,
            fontWeight = FontWeight.Medium
        )
    }
}
