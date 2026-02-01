package com.example.deliveryapp.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryapp.auth.AuthManager
import com.example.deliveryapp.data.model.Order
import com.example.deliveryapp.data.model.OrderItem
import com.example.deliveryapp.data.model.OrderStatus
import com.example.deliveryapp.data.model.Product
import com.example.deliveryapp.data.repository.FirestoreOrderRepository
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
    val cart: List<OrderItem> = emptyList(),
    val orderPlaced: Boolean = false,
    val lastOrderId: String? = null
)

class ProductSearchViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    // Use Firestore repository instead of in-memory
    private val orderRepository = FirestoreOrderRepository.getInstance()
    private val authManager = AuthManager.getInstance()

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

    // ... rest of the code stays the same, but update placeOrder:
    fun placeOrder(deliveryAddress: String) {
        viewModelScope.launch {
            if (_uiState.value.cart.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Cart is empty"
                )
                return@launch
            }

            // Get current user ID
            val userId = authManager.getCurrentUserId() ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "Not logged in"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val order = Order(
                orderId = "ORD${System.currentTimeMillis()}",
                shopperId = userId,  // Use actual user ID
                items = _uiState.value.cart,
                status = OrderStatus.PENDING,
                totalPrice = _uiState.value.cart.sumOf { it.subtotal },
                deliveryAddress = deliveryAddress,
                createdAt = Instant.now()
            )

            orderRepository.createOrder(order)
                .onSuccess { createdOrder ->
                    _uiState.value = _uiState.value.copy(
                        cart = emptyList(),
                        isLoading = false,
                        orderPlaced = true,
                        lastOrderId = createdOrder.orderId
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to place order"
                    )
                }
        }
    }

    fun clearOrderPlacedFlag() {
        _uiState.value = _uiState.value.copy(
            orderPlaced = false,
            lastOrderId = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}