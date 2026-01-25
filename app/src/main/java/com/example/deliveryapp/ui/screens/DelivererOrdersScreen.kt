package com.example.deliveryapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deliveryapp.data.model.Order
import com.example.deliveryapp.data.model.OrderStatus
import com.example.deliveryapp.viewmodel.DelivererOrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelivererOrdersScreen(
    viewModel: DelivererOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Delivery Dashboard") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Error Message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Loading State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Tabs for Available and Assigned Orders
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Available Orders", "My Deliveries")

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Available Orders Tab
                        if (uiState.availableOrders.isEmpty()) {
                            item {
                                Text(
                                    "No available orders",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(uiState.availableOrders) { order ->
                                AvailableOrderCard(
                                    order = order,
                                    onAccept = { viewModel.acceptOrder(order.orderId) }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Assigned Orders Tab
                        if (uiState.assignedOrders.isEmpty()) {
                            item {
                                Text(
                                    "No assigned deliveries",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(uiState.assignedOrders) { order ->
                                AssignedOrderCard(
                                    order = order,
                                    onPickedUp = { viewModel.markAsPickedUp(order.orderId) },
                                    onDelivered = { viewModel.markAsDelivered(order.orderId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableOrderCard(
    order: Order,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
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
                    "${order.totalPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Delivery: ${order.deliveryAddress}")
            Text("Items: ${order.items.size}")
            Text("Status: ${order.status}")

            Spacer(modifier = Modifier.height(8.dp))

            // Item Details
            order.items.forEach { item ->
                Text(
                    "• ${item.quantity}x ${item.productName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accept Delivery")
            }
        }
    }
}

@Composable
fun AssignedOrderCard(
    order: Order,
    onPickedUp: () -> Unit,
    onDelivered: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (order.status) {
                OrderStatus.READY_FOR_PICKUP -> MaterialTheme.colorScheme.tertiaryContainer
                OrderStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.primaryContainer
                OrderStatus.DELIVERED -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
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
                    "${order.totalPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            Surface(
                color = when (order.status) {
                    OrderStatus.READY_FOR_PICKUP -> MaterialTheme.colorScheme.tertiary
                    OrderStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.primary
                    OrderStatus.DELIVERED -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.secondary
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = order.status.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Delivery: ${order.deliveryAddress}")
            Text("Items: ${order.items.size}")

            Spacer(modifier = Modifier.height(8.dp))

            // Item Details
            order.items.forEach { item ->
                Text(
                    "• ${item.quantity}x ${item.productName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons based on status
            when (order.status) {
                OrderStatus.READY_FOR_PICKUP -> {
                    Button(
                        onClick = onPickedUp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Picked Up")
                    }
                }
                OrderStatus.OUT_FOR_DELIVERY -> {
                    Button(
                        onClick = onDelivered,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Delivered")
                    }
                }
                OrderStatus.DELIVERED -> {
                    Text(
                        "✓ Delivered",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                else -> {
                    // No action needed
                }
            }
        }
    }
}