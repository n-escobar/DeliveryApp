package com.example.deliveryapp.data.model

import java.time.Instant

enum class UserRole { SHOPPER, DELIVERER }

enum class OrderStatus {
    PENDING, CONFIRMED, PREPARING,
    READY_FOR_PICKUP, OUT_FOR_DELIVERY,
    DELIVERED, CANCELLED
}

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val category: String
)

data class OrderItem(
    val productId: String,
    val productName: String,
    val priceAtPurchase: Double,
    val quantity: Int
) {
    val subtotal: Double get() = priceAtPurchase * quantity
}

data class Order(
    val orderId: String,
    val shopperId: String,
    val delivererId: String? = null,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val totalPrice: Double,
    val deliveryAddress: String,
    val createdAt: Instant
)