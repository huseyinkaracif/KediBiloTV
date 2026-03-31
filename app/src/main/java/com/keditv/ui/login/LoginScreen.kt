package com.keditv.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.keditv.ui.common.LoadingIndicator
import com.keditv.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    if (state.isCheckingSaved) {
        LoadingIndicator()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Animated cat logo
            AnimatedCatLogo(isLoading = state.isLoading)

            Spacer(modifier = Modifier.height(28.dp))

            // Tab seçici
            val tabs = listOf("Xtream API", "M3U URL")
            val selectedIndex = if (state.loginMode == LoginMode.XTREAM) 0 else 1

            TabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                containerColor = NeonSurface,
                contentColor = NeonCoral,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = NeonCoral
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = {
                            viewModel.setLoginMode(if (index == 0) LoginMode.XTREAM else LoginMode.M3U_URL)
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedIndex == index) NeonCoral else NeonTextSecondary
                            )
                        }
                    )
                }
            }

            // Form alanı
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = NeonSurface,
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (state.loginMode == LoginMode.XTREAM) {
                        NeonTextField(
                            value = state.serverUrl,
                            onValueChange = viewModel::updateServerUrl,
                            label = "Sunucu URL",
                            placeholder = "http://sunucu.com:8080",
                            keyboardType = KeyboardType.Uri
                        )
                        NeonTextField(
                            value = state.username,
                            onValueChange = viewModel::updateUsername,
                            label = "Kullanıcı Adı",
                            placeholder = "kullanici"
                        )
                        NeonTextField(
                            value = state.password,
                            onValueChange = viewModel::updatePassword,
                            label = "Şifre",
                            isPassword = true
                        )
                    } else {
                        NeonTextField(
                            value = state.m3uUrl,
                            onValueChange = viewModel::updateM3uUrl,
                            label = "M3U / Playlist URL",
                            placeholder = "http://sunucu.com:8080/get.php?username=X&password=Y",
                            keyboardType = KeyboardType.Uri
                        )
                        Text(
                            text = "URL içinde username= ve password= parametreleri olmalı.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error — animated fade in
            AnimatedContent(
                targetState = state.error,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "error_anim"
            ) { error ->
                if (error != null) {
                    Text(
                        text = "😿 $error",
                        color = NeonError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                } else {
                    Spacer(Modifier.height(0.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bağlan butonu
            Button(
                onClick = viewModel::login,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCoral,
                    disabledContainerColor = NeonSurfaceHigh
                )
            ) {
                if (state.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = NeonTextPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Bağlanıyor...",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonTextPrimary
                        )
                    }
                } else {
                    Text(
                        text = "Bağlan",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = NeonTextPrimary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Animated Cat Logo
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedCatLogo(isLoading: Boolean) {
    val transition = rememberInfiniteTransition(label = "cat_logo")

    val floatY by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float"
    )

    val eyeGlow by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.offset(y = floatY.dp),
            contentAlignment = Alignment.Center
        ) {
            // Subtle glow behind the emoji
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        NeonCoral.copy(alpha = 0.08f * eyeGlow),
                        androidx.compose.foundation.shape.CircleShape
                    )
            )
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
                label = "cat_face"
            ) { loading ->
                Text(
                    text = if (loading) "😸" else "😺",
                    fontSize = 60.sp
                )
            }
        }

        Text(
            text = "KediTV",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
            color = NeonCoral
        )
        Text(
            text = "IPTV oynatıcın",
            style = MaterialTheme.typography.bodySmall,
            color = NeonTextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Neon Styled TextField
// ─────────────────────────────────────────────────────────────────

@Composable
private fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = NeonTextSecondary) },
        placeholder = { Text(placeholder, color = NeonTextMuted) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = NeonTextPrimary,
            unfocusedTextColor = NeonTextPrimary,
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = NeonSurfaceRim,
            cursorColor = NeonCyan,
            focusedContainerColor = NeonSurfaceHigh,
            unfocusedContainerColor = NeonSurface
        )
    )
}
