package com.keditv.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keditv.domain.model.Category
import com.keditv.domain.model.ContentType
import com.keditv.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val filteredCategories: List<Category> = emptyList(),
    val searchQuery: String = "",
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
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        categories = it,
                        filteredCategories = it,
                        error = null
                    )
                },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun search(query: String) {
        val filtered = if (query.isBlank()) _state.value.categories
        else _state.value.categories.filter { it.name.contains(query, ignoreCase = true) }
        _state.value = _state.value.copy(searchQuery = query, filteredCategories = filtered)
    }

    fun retry() = loadCategories()
}
