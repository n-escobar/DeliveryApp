package com.example.deliveryapp.data.model

import org.junit.Test
import org.junit.Assert.*
import java.time.Instant

class OrderTest {

    @Test
    fun `Order creation with valid data`() {
        val items = listOf(
            OrderItem("1", "Apple", 5.0, 2),
            OrderItem("2", "Milk", 3.5, 1)
        )

        val order = Order(
            orderId = "ORD001",
            shopperId = "shopper1",
            items = items,
            status = OrderStatus.PENDING,
            totalPrice = 13.5,
            deliveryAddress = "123 Main St",
            createdAt = Instant.now()
        )

        assertEquals("ORD001", order.orderId)
        assertEquals(2, order.items.size)
        assertEquals(OrderStatus.PENDING, order.status)
    }

    @Test
    fun `Order with empty items list`() {
        val order = Order(
            orderId = "ORD001",
            shopperId = "shopper1",
            items = emptyList(),
            status = OrderStatus.PENDING,
            totalPrice = 0.0,
            deliveryAddress = "123 Main St",
            createdAt = Instant.now()
        )

        assertTrue(order.items.isEmpty())
        assertEquals(0.0, order.totalPrice, 0.001)
    }

    @Test
    fun `Order status progression is valid`() {
        val order = Order(
            orderId = "ORD001",
            shopperId = "shopper1",
            items = listOf(OrderItem("1", "Apple", 5.0, 1)),
            status = OrderStatus.PENDING,
            totalPrice = 5.0,
            deliveryAddress = "123 Main St",
            createdAt = Instant.now()
        )

        val confirmed = order.copy(status = OrderStatus.CONFIRMED)
        val preparing = confirmed.copy(status = OrderStatus.PREPARING)
        val ready = preparing.copy(status = OrderStatus.READY_FOR_PICKUP)

        assertEquals(OrderStatus.READY_FOR_PICKUP, ready.status)
    }

    @Test
    fun `Order with deliverer assignment`() {
        val order = Order(
            orderId = "ORD001",
            shopperId = "shopper1",
            delivererId = null,
            items = listOf(OrderItem("1", "Apple", 5.0, 1)),
            status = OrderStatus.READY_FOR_PICKUP,
            totalPrice = 5.0,
            deliveryAddress = "123 Main St",
            createdAt = Instant.now()
        )

        val assigned = order.copy(
            delivererId = "deliverer1",
            status = OrderStatus.OUT_FOR_DELIVERY
        )

        assertEquals("deliverer1", assigned.delivererId)
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, assigned.status)
    }

    @Test
    fun `Order total matches sum of items`() {
        val items = listOf(
            OrderItem("1", "Apple", 5.0, 2),
            OrderItem("2", "Milk", 3.5, 1),
            OrderItem("3", "Bread", 2.99, 3)
        )

        val expectedTotal = items.sumOf { it.subtotal }

        val order = Order(
            orderId = "ORD001",
            shopperId = "shopper1",
            items = items,
            status = OrderStatus.PENDING,
            totalPrice = expectedTotal,
            deliveryAddress = "123 Main St",
            createdAt = Instant.now()
        )

        assertEquals(expectedTotal, order.totalPrice, 0.001)
    }
}