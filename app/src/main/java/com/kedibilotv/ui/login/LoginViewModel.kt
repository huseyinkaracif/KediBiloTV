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

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
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
                _state.value = _state.value.copy(isCheckingSaved = false)
            }
        }
    }

    fun updateServerUrl(url: String) { _state.value = _state.value.copy(serverUrl = url, error = null) }
    fun updateUsername(name: String) { _state.value = _state.value.copy(username = name, error = null) }
    fun updatePassword(pass: String) { _state.value = _state.value.copy(password = pass, error = null) }

    fun login() {
        val s = _state.value
        if (s.serverUrl.isBlank() || s.username.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Tum alanlari doldur")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            val result = loginUseCase(s.serverUrl, s.username, s.password)
            result.fold(
                onSuccess = { _state.value = _state.value.copy(isLoading = false, isLoggedIn = true) },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }
}
