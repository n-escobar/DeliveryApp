package com.example.deliveryapp.data.model

import org.junit.Assert.*
import org.junit.Test

class OrderItemTest {

    @Test
    fun `subtotal calculation is correct for single quantity`() {
        // Arrange
        val item = OrderItem(
            productId = "1",
            productName = "Apple",
            priceAtPurchase = 5.0,
            quantity = 1
        )

        // Act
        val result = item.subtotal

        // Assert
        assertEquals(5.0, result, 0.001)
    }

    @Test
    fun `subtotal calculation is correct for multiple quantities`() {
        val item = OrderItem(
            productId = "1",
            productName = "Apple",
            priceAtPurchase = 5.0,
            quantity = 3
        )

        assertEquals(15.0, item.subtotal, 0.001)
    }

    @Test
    fun `subtotal handles decimal prices correctly`() {
        val item = OrderItem(
            productId = "1",
            productName = "Milk",
            priceAtPurchase = 3.49,
            quantity = 2
        )

        assertEquals(6.98, item.subtotal, 0.001)
    }

    @Test
    fun `subtotal is zero when quantity is zero`() {
        val item = OrderItem(
            productId = "1",
            productName = "Apple",
            priceAtPurchase = 5.0,
            quantity = 0
        )

        assertEquals(0.0, item.subtotal, 0.001)
    }
}