package com.kedibilotv.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedibilotv.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BufferSize(val label: String, val minBufferMs: Int, val maxBufferMs: Int) {
    LOW("Dusuk", 5_000, 15_000),
    MEDIUM("Orta", 10_000, 30_000),
    HIGH("Yuksek", 20_000, 60_000)
}

data class SettingsUiState(
    val isLoggedOut: Boolean = false,
    val bufferSize: BufferSize = BufferSize.MEDIUM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    fun setBufferSize(size: BufferSize) {
        _state.value = _state.value.copy(bufferSize = size)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
}
