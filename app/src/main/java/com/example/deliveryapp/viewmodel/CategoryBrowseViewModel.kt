package com.example.deliveryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryapp.data.model.Product
import com.example.deliveryapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryBrowseUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CategoryBrowseViewModel : ViewModel() {
    private val productRepository = ProductRepository()

    private val _uiState = MutableStateFlow(CategoryBrowseUiState())
    val uiState: StateFlow<CategoryBrowseUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            productRepository.getCategories()
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false
                    )
                    // Optionally load first category automatically
                    if (categories.isNotEmpty() && _uiState.value.selectedCategory == null) {
                        selectCategory(categories.first())
                    }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedCategory = category,
                isLoading = true,
                error = null
            )

            productRepository.getProductsByCategory(category)
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}