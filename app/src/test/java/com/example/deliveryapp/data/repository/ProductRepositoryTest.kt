package com.example.deliveryapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ProductRepositoryTest {

    private lateinit var repository: ProductRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = ProductRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchProducts with empty query returns all products`() = runTest {
        val result = repository.searchProducts("")

        assertTrue(result.isSuccess)
        val products = result.getOrNull() ?: emptyList()
        assertTrue(products.isNotEmpty())
        assertTrue(products.size >= 25)  // We know there are 25 products
    }

    @Test
    fun `searchProducts filters by name case-insensitive`() = runTest {
        val result = repository.searchProducts("apple")

        assertTrue(result.isSuccess)
        val products = result.getOrNull() ?: emptyList()
        assertTrue(products.all {
            it.name.contains("apple", ignoreCase = true) ||
                    it.category.contains("apple", ignoreCase = true)
        })
    }

    @Test
    fun `searchProducts filters by category`() = runTest {
        val result = repository.searchProducts("Fruits")

        assertTrue(result.isSuccess)
        val products = result.getOrNull() ?: emptyList()
        assertTrue(products.any { it.category == "Fruits" })
    }

    @Test
    fun `searchProducts returns empty for non-existent product`() = runTest {
        val result = repository.searchProducts("NonExistentProduct12345")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `getCategories returns unique categories`() = runTest {
        val result = repository.getCategories()

        assertTrue(result.isSuccess)
        val categories = result.getOrNull() ?: emptyList()
        assertTrue(categories.isNotEmpty())
        assertEquals(categories.size, categories.distinct().size)  // All unique
    }

    @Test
    fun `getCategories returns sorted categories`() = runTest {
        val result = repository.getCategories()

        assertTrue(result.isSuccess)
        val categories = result.getOrNull() ?: emptyList()
        assertEquals(categories, categories.sorted())
    }

    @Test
    fun `getProductsByCategory returns correct products`() = runTest {
        val category = "Fruits"
        val result = repository.getProductsByCategory(category)

        assertTrue(result.isSuccess)
        val products = result.getOrNull() ?: emptyList()
        assertTrue(products.all { it.category == category })
    }

    @Test
    fun `getProductsByCategory case-insensitive`() = runTest {
        val result = repository.getProductsByCategory("fruits")

        assertTrue(result.isSuccess)
        val products = result.getOrNull() ?: emptyList()
        assertTrue(products.isNotEmpty())
    }

    @Test
    fun `getProductsByCategory returns empty for invalid category`() = runTest {
        val result = repository.getProductsByCategory("InvalidCategory")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `getAllProductsGroupedByCategory groups correctly`() = runTest {
        val result = repository.getAllProductsGroupedByCategory()

        assertTrue(result.isSuccess)
        val grouped = result.getOrNull() ?: emptyMap()

        // Check that all products in each category match the key
        grouped.forEach { (category, products) ->
            assertTrue(products.all { it.category == category })
        }
    }

    @Test
    fun `getFeaturedProducts returns exactly 6 products`() = runTest {
        val result = repository.getFeaturedProducts()

        assertTrue(result.isSuccess)
        assertEquals(6, result.getOrNull()?.size)
    }

    @Test
    fun `getFeaturedProducts returns first 6 products in order`() = runTest {
        val allProducts = repository.searchProducts("").getOrNull() ?: emptyList()
        val featured = repository.getFeaturedProducts().getOrNull() ?: emptyList()

        assertEquals(allProducts.take(6), featured)
    }
}