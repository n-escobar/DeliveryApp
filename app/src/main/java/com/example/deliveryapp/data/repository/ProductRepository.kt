package com.example.deliveryapp.data.repository

import com.example.deliveryapp.data.model.Product
import kotlinx.coroutines.delay

class ProductRepository {

    private val allProducts = listOf(
        // Fruits
        Product("1", "Organic Apples", 4.99, "", "Fruits"),
        Product("5", "Organic Bananas", 2.99, "", "Fruits"),
        Product("6", "Fresh Strawberries", 5.49, "", "Fruits"),
        Product("7", "Seedless Grapes", 3.99, "", "Fruits"),
        Product("8", "Ripe Avocados", 6.99, "", "Fruits"),

        // Dairy
        Product("2", "Whole Milk", 3.49, "", "Dairy"),
        Product("4", "Free Range Eggs", 6.99, "", "Dairy"),
        Product("9", "Greek Yogurt", 4.49, "", "Dairy"),
        Product("10", "Cheddar Cheese", 5.99, "", "Dairy"),
        Product("11", "Butter", 4.29, "", "Dairy"),

        // Bakery
        Product("3", "Sourdough Bread", 5.99, "", "Bakery"),
        Product("12", "Croissants", 7.99, "", "Bakery"),
        Product("13", "Bagels", 4.99, "", "Bakery"),
        Product("14", "Whole Wheat Bread", 5.49, "", "Bakery"),
        Product("15", "Cinnamon Rolls", 6.99, "", "Bakery"),

        // Vegetables
        Product("16", "Organic Carrots", 2.99, "", "Vegetables"),
        Product("17", "Fresh Broccoli", 3.49, "", "Vegetables"),
        Product("18", "Bell Peppers", 4.99, "", "Vegetables"),
        Product("19", "Cherry Tomatoes", 3.99, "", "Vegetables"),
        Product("20", "Fresh Spinach", 3.49, "", "Vegetables"),

        // Meat & Seafood
        Product("21", "Chicken Breast", 8.99, "", "Meat & Seafood"),
        Product("22", "Ground Beef", 9.99, "", "Meat & Seafood"),
        Product("23", "Salmon Fillet", 12.99, "", "Meat & Seafood"),
        Product("24", "Pork Chops", 10.99, "", "Meat & Seafood"),
        Product("25", "Shrimp", 14.99, "", "Meat & Seafood")
    )

    /**
     * Search products by name or category
     */
    suspend fun searchProducts(query: String): Result<List<Product>> {
        delay(500) // Simulate network delay

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

    /**
     * Get all unique categories
     */
    suspend fun getCategories(): Result<List<String>> {
        delay(300)
        val categories = allProducts
            .map { it.category }
            .distinct()
            .sorted()
        return Result.success(categories)
    }

    /**
     * Get products for a specific category
     */
    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        delay(400)
        val products = allProducts.filter {
            it.category.equals(category, ignoreCase = true)
        }
        return Result.success(products)
    }

    /**
     * Get all products grouped by category
     * Returns a map where key = category name, value = list of products
     */
    suspend fun getAllProductsGroupedByCategory(): Result<Map<String, List<Product>>> {
        delay(500)
        val grouped = allProducts.groupBy { it.category }
        return Result.success(grouped)
    }

    /**
     * Get featured/popular products (can be used for home screen)
     */
    suspend fun getFeaturedProducts(): Result<List<Product>> {
        delay(300)
        // For now, return first 6 products
        // In real app, this would be based on sales data, ratings, etc.
        val featured = allProducts.take(6)
        return Result.success(featured)
    }
}