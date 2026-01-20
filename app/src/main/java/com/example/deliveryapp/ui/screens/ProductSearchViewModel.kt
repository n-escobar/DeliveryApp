package com.example.deliveryapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryapp.data.model.*
import com.example.deliveryapp.data.repository.OrderRepository
import com.example.deliveryapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class ProductSearchUiState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val cart: List<OrderItem> = emptyList()
)

class ProductSearchViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow(ProductSearchUiState())
    val uiState: StateFlow<ProductSearchUiState> = _uiState.asStateFlow()

    init {
        searchProducts("")
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchProducts(query)
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            productRepository.searchProducts(query)
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

    fun addToCart(product: Product, quantity: Int) {
        val orderItem = OrderItem(
            productId = product.id,
            productName = product.name,
            priceAtPurchase = product.price,
            quantity = quantity
        )

        val currentCart = _uiState.value.cart.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.productId == product.id }

        if (existingIndex >= 0) {
            currentCart[existingIndex] = currentCart[existingIndex].copy(
                quantity = currentCart[existingIndex].quantity + quantity
            )
        } else {
            currentCart.add(orderItem)
        }

        _uiState.value = _uiState.value.copy(cart = currentCart)
    }

    fun placeOrder(shopperId: String, deliveryAddress: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val order = Order(
                orderId = "ORD${System.currentTimeMillis()}",
                shopperId = shopperId,
                items = _uiState.value.cart,
                status = OrderStatus.PENDING,
                totalPrice = _uiState.value.cart.sumOf { it.subtotal },
                deliveryAddress = deliveryAddress,
                createdAt = Instant.now()
            )

            orderRepository.createOrder(order)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        cart = emptyList(),
                        isLoading = false
                    )
                }
        }
    }
}