package com.keditv.ui.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keditv.domain.model.ContentItem
import com.keditv.domain.model.ContentType
import com.keditv.domain.usecase.GetContentListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentListUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<ContentItem> = emptyList(),
    val filteredItems: List<ContentItem> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class ContentListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContentListUseCase: GetContentListUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val categoryId = savedStateHandle.get<String>("categoryId")!!

    private val _state = MutableStateFlow(ContentListUiState())
    val state: StateFlow<ContentListUiState> = _state

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getContentListUseCase(type, categoryId).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false, items = it, filteredItems = it, error = null)
                },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun search(query: String) {
        val filtered = if (query.isBlank()) _state.value.items
        else _state.value.items.filter { it.name.contains(query, ignoreCase = true) }
        _state.value = _state.value.copy(searchQuery = query, filteredItems = filtered)
    }

    fun retry() = load()
}
