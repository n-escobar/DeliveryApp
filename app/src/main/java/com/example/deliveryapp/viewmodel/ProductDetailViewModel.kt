package com.example.deliveryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryapp.data.model.Product
import com.example.deliveryapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel for Product Detail
data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val quantity: Int = 1
)

class ProductDetailViewModel(
    private val productId: String
) : ViewModel() {
    private val productRepository = ProductRepository()

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            productRepository.searchProducts("")
                .onSuccess { products ->
                    val product = products.find { it.id == productId }
                    _uiState.value = _uiState.value.copy(
                        product = product,
                        isLoading = false,
                        error = if (product == null) "Product not found" else null
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

    fun incrementQuantity() {
        _uiState.value = _uiState.value.copy(
            quantity = _uiState.value.quantity + 1
        )
    }

    fun decrementQuantity() {
        val currentQuantity = _uiState.value.quantity
        if (currentQuantity > 1) {
            _uiState.value = _uiState.value.copy(
                quantity = currentQuantity - 1
            )
        }
    }

    fun resetQuantity() {
        _uiState.value = _uiState.value.copy(quantity = 1)
    }
}