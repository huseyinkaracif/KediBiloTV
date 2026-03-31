package com.keditv.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        }
    }
}
