package com.kedibilotv.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kedibilotv.ui.theme.NeonCyan
import com.kedibilotv.ui.theme.NeonTextSecondary

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "𝄞 🐾", fontSize = 52.sp)
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = NeonTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Text(
                text = "— KediBilo",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan.copy(alpha = 0.6f)
            )
        }
    }
}
