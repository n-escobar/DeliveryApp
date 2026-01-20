package com.example.deliveryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopperOrdersScreen(
    viewModel: ShopperOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Orders") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.orders) { order ->
                OrderCard(
                    order = order,
                    onCancel = { viewModel.cancelOrder(order.orderId) }
                )
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order #${order.orderId}", style = MaterialTheme.typography.titleMedium)
            Text("Status: ${order.status}")
            Text("Total: $${order.totalPrice}")
            Text("Items: ${order.items.size}")

            if (order.status == OrderStatus.PENDING) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Cancel Order")
                }
            }
        }
    }
}