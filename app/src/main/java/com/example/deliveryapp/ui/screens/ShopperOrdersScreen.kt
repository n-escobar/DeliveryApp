package com.example.deliveryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deliveryapp.data.model.Order
import com.example.deliveryapp.data.model.OrderStatus
import com.example.deliveryapp.viewmodel.ShopperOrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopperOrdersScreen(
    viewModel: ShopperOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Reload orders when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                actions = {
                    // Add refresh button
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No orders yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start shopping to create your first order!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
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