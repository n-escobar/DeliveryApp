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

    // KEY FIX: Reload orders every time the screen appears
    // Use a key that changes to force reload
    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    // ADDITIONAL FIX: Add a DisposableEffect to reload when returning to this screen
    DisposableEffect(Unit) {
        viewModel.loadOrders()
        onDispose { }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Order #${order.orderId}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "$${String.format("%.2f", order.totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            Surface(
                color = when (order.status) {
                    OrderStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                    OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
                    OrderStatus.PREPARING -> MaterialTheme.colorScheme.secondary
                    OrderStatus.READY_FOR_PICKUP -> MaterialTheme.colorScheme.primaryContainer
                    OrderStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.primary
                    OrderStatus.DELIVERED -> MaterialTheme.colorScheme.outline
                    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = order.status.toString().replace("_", " "),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (order.status) {
                        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Delivery: ${order.deliveryAddress}")
            Text("Items: ${order.items.size}")

            // Show item details
            order.items.forEach { item ->
                Text(
                    "• ${item.quantity}x ${item.productName} - $${String.format("%.2f", item.subtotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            if (order.status == OrderStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Order")
                }
            }
        }
    }
}