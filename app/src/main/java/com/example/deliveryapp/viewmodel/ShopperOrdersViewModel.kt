package com.example.deliveryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryapp.data.model.Order
import com.example.deliveryapp.data.model.OrderStatus
import com.example.deliveryapp.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShopperOrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false
)

class ShopperOrdersViewModel : ViewModel() {
    private val orderRepository = OrderRepository()

    private val _uiState = MutableStateFlow(ShopperOrdersUiState())
    val uiState: StateFlow<ShopperOrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            orderRepository.getOrdersForShopper("shopper1")
                .onSuccess { orders ->
                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        isLoading = false
                    )
                }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
                .onSuccess { loadOrders() }
        }
    }
}