package com.kedibilotv.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedibilotv.domain.repository.AuthRepository
import com.kedibilotv.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginMode { XTREAM, M3U_URL }

data class LoginUiState(
    val loginMode: LoginMode = LoginMode.XTREAM,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val m3uUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isCheckingSaved: Boolean = true
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    init {
        checkSavedLogin()
    }

    private fun checkSavedLogin() {
        viewModelScope.launch {
            try {
                val config = authRepository.getSavedConfig()
                if (config != null) {
                    _state.value = _state.value.copy(
                        serverUrl = config.serverUrl,
                        username = config.username,
                        password = config.password,
                        isLoggedIn = true,
                        isCheckingSaved = false
                    )
                } else {
                    _state.value = _state.value.copy(isCheckingSaved = false)
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "checkSavedConfig Hata:", e)
                _state.value = _state.value.copy(isCheckingSaved = false)
            }
        }
    }

    fun setLoginMode(mode: LoginMode) { _state.value = _state.value.copy(loginMode = mode, error = null) }
    fun updateServerUrl(url: String) { _state.value = _state.value.copy(serverUrl = url, error = null) }
    fun updateUsername(name: String) { _state.value = _state.value.copy(username = name, error = null) }
    fun updatePassword(pass: String) { _state.value = _state.value.copy(password = pass, error = null) }
    fun updateM3uUrl(url: String) { _state.value = _state.value.copy(m3uUrl = url, error = null) }

    fun login() {
        val s = _state.value
        if (s.loginMode == LoginMode.M3U_URL) {
            loginWithM3uUrl(s.m3uUrl)
        } else {
            loginWithXtream(s.serverUrl, s.username, s.password)
        }
    }

    private fun loginWithXtream(serverUrl: String, username: String, password: String) {
        if (serverUrl.isBlank() || username.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Tum alanlari doldur")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = loginUseCase(serverUrl.trim(), username.trim(), password.trim())
            result.fold(
                onSuccess = { _state.value = _state.value.copy(isLoading = false, isLoggedIn = true) },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    private fun loginWithM3uUrl(rawUrl: String) {
        if (rawUrl.isBlank()) {
            _state.value = _state.value.copy(error = "URL bos olamaz")
            return
        }
        val parsed = parseM3uUrl(rawUrl.trim())
        if (parsed == null) {
            _state.value = _state.value.copy(error = "Gecersiz URL. username ve password parametresi olmali.")
            return
        }
        val (serverUrl, username, password) = parsed
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = loginUseCase(serverUrl, username, password)
            result.fold(
                onSuccess = { _state.value = _state.value.copy(isLoading = false, isLoggedIn = true) },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    private fun parseM3uUrl(url: String): Triple<String, String, String>? {
        return try {
            val uri = android.net.Uri.parse(url)
            val username = uri.getQueryParameter("username")?.trim() ?: return null
            val password = uri.getQueryParameter("password")?.trim() ?: return null
            val serverUrl = buildString {
                append(uri.scheme).append("://").append(uri.authority)
                if (!uri.path.isNullOrBlank()) append(uri.path)
            }
            Triple(serverUrl, username, password)
        } catch (e: Exception) {
            null
        }
    }
}

