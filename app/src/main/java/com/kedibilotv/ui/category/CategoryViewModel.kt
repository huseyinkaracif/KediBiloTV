package com.kedibilotv.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedibilotv.domain.model.Category
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val contentType: ContentType = ContentType.LIVE
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val _state = MutableStateFlow(CategoryUiState(contentType = type))
    val state: StateFlow<CategoryUiState> = _state

    init { loadCategories() }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getCategoriesUseCase(type).fold(
                onSuccess = { _state.value = _state.value.copy(isLoading = false, categories = it, error = null) },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun retry() = loadCategories()
}
