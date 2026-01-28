package com.example.deliveryapp.data.repository

import com.example.deliveryapp.data.model.*
import kotlinx.coroutines.delay
import java.time.Instant

class OrderRepository private constructor() {

    companion object {
        @Volatile
        private var instance: OrderRepository? = null

        fun getInstance(): OrderRepository {
            return instance ?: synchronized(this) {
                instance ?: OrderRepository().also { instance = it }
            }
        }
    }

    private val orders = mutableListOf(
        // Shopper orders
        Order(
            orderId = "ORD001",
            shopperId = "shopper1",
            delivererId = null,
            items = listOf(
                OrderItem("1", "Organic Apples", 4.99, 2)
            ),
            status = OrderStatus.PENDING,
            totalPrice = 9.98,
            deliveryAddress = "123 Main St",
            createdAt = Instant.now()
        ),
        // Available for delivery
        Order(
            orderId = "ORD002",
            shopperId = "shopper2",
            delivererId = null,
            items = listOf(
                OrderItem("2", "Whole Milk", 3.49, 1),
                OrderItem("3", "Sourdough Bread", 5.99, 1)
            ),
            status = OrderStatus.READY_FOR_PICKUP,
            totalPrice = 9.48,
            deliveryAddress = "456 Oak Ave",
            createdAt = Instant.now()
        ),
        Order(
            orderId = "ORD003",
            shopperId = "shopper3",
            delivererId = null,
            items = listOf(
                OrderItem("4", "Free Range Eggs", 6.99, 2)
            ),
            status = OrderStatus.READY_FOR_PICKUP,
            totalPrice = 13.98,
            deliveryAddress = "789 Pine Rd",
            createdAt = Instant.now()
        ),
        // Already assigned to deliverer1
        Order(
            orderId = "ORD004",
            shopperId = "shopper4",
            delivererId = "deliverer1",
            items = listOf(
                OrderItem("5", "Organic Bananas", 2.99, 3)
            ),
            status = OrderStatus.OUT_FOR_DELIVERY,
            totalPrice = 8.97,
            deliveryAddress = "321 Elm St",
            createdAt = Instant.now()
        )
    )

    suspend fun getOrdersForShopper(shopperId: String): Result<List<Order>> {
        delay(300)
        val shopperOrders = orders.filter { it.shopperId == shopperId }
        shopperOrders.forEach {
            println("  - Order ${it.orderId}: ${it.status}, items: ${it.items.size}")
        }
        return Result.success(shopperOrders)
    }

    suspend fun createOrder(order: Order): Result<Order> {
        delay(500)
        orders.add(order)
        return Result.success(order)
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Order> {
        delay(300)
        val index = orders.indexOfFirst { it.orderId == orderId }
        if (index == -1) return Result.failure(Exception("Order not found"))

        val updated = orders[index].copy(status = newStatus)
        orders[index] = updated
        return Result.success(updated)
    }

    suspend fun getAvailableOrdersForDelivery(): Result<List<Order>> {
        delay(300)
        val available = orders.filter {
            it.status == OrderStatus.READY_FOR_PICKUP && it.delivererId == null
        }
        return Result.success(available)
    }

    suspend fun getOrdersForDeliverer(delivererId: String): Result<List<Order>> {
        delay(300)
        val delivererOrders = orders.filter { it.delivererId == delivererId }
        return Result.success(delivererOrders)
    }

    suspend fun assignDeliverer(orderId: String, delivererId: String): Result<Order> {
        delay(300)
        val index = orders.indexOfFirst { it.orderId == orderId }

        if (index == -1) {
            return Result.failure(Exception("Order not found"))
        }

        val updated = orders[index].copy(
            delivererId = delivererId,
            status = OrderStatus.OUT_FOR_DELIVERY
        )
        orders[index] = updated
        return Result.success(updated)
    }

    /**
     * Get all orders that need preparation (not yet ready for pickup)
     * These are orders that deliverer needs to prepare
     */
    suspend fun getPendingOrders(): Result<List<Order>> {
        delay(300)
        val pending = orders.filter {
            it.status in listOf(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.PREPARING
            )
        }
        return Result.success(pending)
    }

    /**
     * Get all orders regardless of status (for admin/management view)
     */
    suspend fun getAllOrders(): Result<List<Order>> {
        delay(300)
        return Result.success(orders.toList())
    }
}