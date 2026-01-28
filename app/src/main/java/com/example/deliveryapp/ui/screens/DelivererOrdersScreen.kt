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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelivererOrdersScreen(
    viewModel: DelivererOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Auto-refresh every 5 seconds to catch new orders
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadOrders()
            delay(5000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Delivery Dashboard")
                        Text(
                            "Auto-refreshing every 5s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
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

            // Three tabs: Preparation, Available, My Deliveries
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf(
                "Preparation (${uiState.pendingOrders.size})",
                "Available (${uiState.availableOrders.size})",
                "My Deliveries (${uiState.assignedOrders.size})"
            )

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1) }
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
                        // Preparation Tab - Orders that need to be prepared
                        if (uiState.pendingOrders.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No orders to prepare",
                                    message = "New orders from shoppers will appear here"
                                )
                            }
                        } else {
                            items(uiState.pendingOrders) { order ->
                                PreparationOrderCard(
                                    order = order,
                                    onConfirm = { viewModel.confirmOrder(order.orderId) },
                                    onStartPreparing = { viewModel.startPreparing(order.orderId) },
                                    onMarkReady = { viewModel.markReadyForPickup(order.orderId) }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Available Orders Tab - Ready for pickup
                        if (uiState.availableOrders.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No available orders",
                                    message = "Orders marked as ready will appear here for delivery"
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
                    2 -> {
                        // Assigned Orders Tab
                        if (uiState.assignedOrders.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No assigned deliveries",
                                    message = "Accept orders from the Available tab to start delivering"
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
fun EmptyStateCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PreparationOrderCard(
    order: Order,
    onConfirm: () -> Unit,
    onStartPreparing: () -> Unit,
    onMarkReady: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (order.status) {
                OrderStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
                OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.secondaryContainer
                OrderStatus.PREPARING -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    // Status Badge
                    Surface(
                        color = when (order.status) {
                            OrderStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                            OrderStatus.CONFIRMED -> MaterialTheme.colorScheme.secondary
                            OrderStatus.PREPARING -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = when (order.status) {
                                OrderStatus.PENDING -> "🆕 NEW ORDER"
                                OrderStatus.CONFIRMED -> "✓ CONFIRMED"
                                OrderStatus.PREPARING -> "👨‍🍳 PREPARING"
                                else -> order.status.toString()
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Text(
                    "$${String.format("%.2f", order.totalPrice)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Details
            Text("📍 ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("📦 ${order.items.size} items", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            // Item List
            Text(
                "Items to prepare:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            order.items.forEach { item ->
                Text(
                    "• ${item.quantity}x ${item.productName}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons based on status
            when (order.status) {
                OrderStatus.PENDING -> {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirm Order")
                    }
                }
                OrderStatus.CONFIRMED -> {
                    Button(
                        onClick = onStartPreparing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Preparing")
                    }
                }
                OrderStatus.PREPARING -> {
                    Button(
                        onClick = onMarkReady,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Ready for Pickup")
                    }
                }
                else -> {}
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "✓ READY FOR PICKUP",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
                Text(
                    "$${String.format("%.2f", order.totalPrice)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("📍 ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
            Text("📦 ${order.items.size} items", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

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
                    "$${String.format("%.2f", order.totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = when (order.status) {
                    OrderStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.primary
                    OrderStatus.DELIVERED -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.secondary
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (order.status) {
                        OrderStatus.OUT_FOR_DELIVERY -> "🚚 OUT FOR DELIVERY"
                        OrderStatus.DELIVERED -> "✓ DELIVERED"
                        else -> order.status.toString()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("📍 ${order.deliveryAddress}")
            Text("📦 ${order.items.size} items")

            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Text(
                    "• ${item.quantity}x ${item.productName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        "✓ Completed",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                else -> {}
            }
        }
    }
}