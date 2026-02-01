package com.example.deliveryapp.data.model

import org.junit.Test
import org.junit.Assert.*

class ProductTest {

    @Test
    fun `Product creation with valid data`() {
        val product = Product(
            id = "1",
            name = "Organic Apples",
            price = 4.99,
            imageUrl = "",
            category = "Fruits"
        )

        assertEquals("1", product.id)
        assertEquals("Organic Apples", product.name)
        assertEquals(4.99, product.price, 0.001)
        assertEquals("Fruits", product.category)
    }

    @Test
    fun `Product with zero price`() {
        val product = Product(
            id = "1",
            name = "Free Sample",
            price = 0.0,
            imageUrl = "",
            category = "Samples"
        )

        assertEquals(0.0, product.price, 0.001)
    }

    @Test
    fun `Product equality based on id`() {
        val product1 = Product("1", "Apple", 5.0, "", "Fruits")
        val product2 = Product("1", "Apple", 5.0, "", "Fruits")

        assertEquals(product1, product2)
    }
}