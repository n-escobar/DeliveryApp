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

data class DelivererOrdersUiState(
    val assignedOrders: List<Order> = emptyList(),
    val availableOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DelivererOrdersViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val currentDelivererId = "deliverer1"

    private val _uiState = MutableStateFlow(DelivererOrdersUiState())
    val uiState: StateFlow<DelivererOrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val availableResult = orderRepository.getAvailableOrdersForDelivery()
            val assignedResult = orderRepository.getOrdersForDeliverer(currentDelivererId)

            if (availableResult.isSuccess && assignedResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    availableOrders = availableResult.getOrNull() ?: emptyList(),
                    assignedOrders = assignedResult.getOrNull() ?: emptyList(),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load orders"
                )
            }
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.assignDeliverer(orderId, currentDelivererId)
                .onSuccess { loadOrders() }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message
                    )
                }
        }
    }

    fun markAsPickedUp(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY)
    }

    fun markAsDelivered(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.DELIVERED)
    }

    private fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
                .onSuccess { loadOrders() }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message
                    )
                }
        }
    }
}