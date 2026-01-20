package com.example.deliveryapp.data.repository

import com.example.deliveryapp.data.model.Product
import kotlinx.coroutines.delay

class ProductRepository {
    suspend fun searchProducts(query: String): Result<List<Product>> {
        delay(500) // Simulate network delay

        val allProducts = listOf(
            Product("1", "Organic Apples", 4.99, "", "Fruits"),
            Product("2", "Whole Milk", 3.49, "", "Dairy"),
            Product("3", "Sourdough Bread", 5.99, "", "Bakery"),
            Product("4", "Free Range Eggs", 6.99, "", "Dairy"),
            Product("5", "Organic Bananas", 2.99, "", "Fruits")
        )

        val filtered = if (query.isBlank()) {
            allProducts
        } else {
            allProducts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }

        return Result.success(filtered)
    }
}