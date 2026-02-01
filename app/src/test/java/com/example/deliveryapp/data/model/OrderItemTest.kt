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

    @Test
    fun `subtotal handles negative quantity - edge case`() {
        val item = OrderItem(
            productId = "1",
            productName = "Apple",
            priceAtPurchase = 5.0,
            quantity = -1
        )

        // Should be negative (business logic decision needed)
        assertEquals(-5.0, item.subtotal, 0.001)
    }

    @Test
    fun `subtotal with zero price`() {
        val item = OrderItem(
            productId = "1",
            productName = "Free Sample",
            priceAtPurchase = 0.0,
            quantity = 5
        )

        assertEquals(0.0, item.subtotal, 0.001)
    }

    @Test
    fun `subtotal with very large quantity`() {
        val item = OrderItem(
            productId = "1",
            productName = "Bulk Item",
            priceAtPurchase = 1.99,
            quantity = 1000
        )

        assertEquals(1990.0, item.subtotal, 0.001)
    }

    @Test
    fun `subtotal handles precision correctly`() {
        val item = OrderItem(
            productId = "1",
            productName = "Precision Test",
            priceAtPurchase = 0.33,
            quantity = 3
        )

        // Should be 0.99, not 0.9899999...
        assertEquals(0.99, item.subtotal, 0.001)
    }

    @Test
    fun `OrderItem equality test`() {
        val item1 = OrderItem("1", "Apple", 5.0, 2)
        val item2 = OrderItem("1", "Apple", 5.0, 2)

        assertEquals(item1, item2)
    }

    @Test
    fun `OrderItem copy with modified quantity`() {
        val original = OrderItem("1", "Apple", 5.0, 2)
        val modified = original.copy(quantity = 3)

        assertEquals(3, modified.quantity)
        assertEquals(15.0, modified.subtotal, 0.001)
    }
}