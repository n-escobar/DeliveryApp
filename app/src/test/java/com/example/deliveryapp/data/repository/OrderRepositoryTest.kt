package com.example.deliveryapp.data.repository

import com.example.deliveryapp.data.model.*
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
import java.time.Instant

@ExperimentalCoroutinesApi
class OrderRepositoryTest {

    private lateinit var repository: OrderRepository

    // Use TestCoroutineDispatcher for testing coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Note: Since OrderRepository is a singleton, we need to handle this carefully
        // In production, you'd want to make it mockable or use dependency injection
        repository = OrderRepository.getInstance()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createOrder adds order successfully`() = runTest {
        val order = Order(
            orderId = "TEST001",
            shopperId = "shopper1",
            items = listOf(OrderItem("1", "Apple", 5.0, 1)),
            status = OrderStatus.PENDING,
            totalPrice = 5.0,
            deliveryAddress = "123 Test St",
            createdAt = Instant.now()
        )

        val result = repository.createOrder(order)

        assertTrue(result.isSuccess)
        assertEquals("TEST001", result.getOrNull()?.orderId)
    }

    @Test
    fun `getOrdersForShopper returns only shopper's orders`() = runTest {
        val shopperId = "shopper1"

        val result = repository.getOrdersForShopper(shopperId)

        assertTrue(result.isSuccess)
        val orders = result.getOrNull() ?: emptyList()
        assertTrue(orders.all { it.shopperId == shopperId })
    }

    @Test
    fun `getOrdersForShopper returns empty list for non-existent shopper`() = runTest {
        val result = repository.getOrdersForShopper("non_existent_shopper")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `updateOrderStatus changes status successfully`() = runTest {
        val orderId = "ORD001"
        val newStatus = OrderStatus.CONFIRMED

        val result = repository.updateOrderStatus(orderId, newStatus)

        assertTrue(result.isSuccess)
        assertEquals(newStatus, result.getOrNull()?.status)
    }

    @Test
    fun `updateOrderStatus fails for non-existent order`() = runTest {
        val result = repository.updateOrderStatus("NON_EXISTENT", OrderStatus.CONFIRMED)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getAvailableOrdersForDelivery returns only ready orders without deliverer`() = runTest {
        val result = repository.getAvailableOrdersForDelivery()

        assertTrue(result.isSuccess)
        val orders = result.getOrNull() ?: emptyList()
        assertTrue(orders.all {
            it.status == OrderStatus.READY_FOR_PICKUP && it.delivererId == null
        })
    }

    @Test
    fun `assignDeliverer updates order with deliverer and status`() = runTest {
        val orderId = "ORD002"  // An available order
        val delivererId = "deliverer1"

        val result = repository.assignDeliverer(orderId, delivererId)

        assertTrue(result.isSuccess)
        assertEquals(delivererId, result.getOrNull()?.delivererId)
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, result.getOrNull()?.status)
    }

    @Test
    fun `assignDeliverer fails for non-existent order`() = runTest {
        val result = repository.assignDeliverer("NON_EXISTENT", "deliverer1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getOrdersForDeliverer returns only assigned orders`() = runTest {
        val delivererId = "deliverer1"

        val result = repository.getOrdersForDeliverer(delivererId)

        assertTrue(result.isSuccess)
        val orders = result.getOrNull() ?: emptyList()
        assertTrue(orders.all { it.delivererId == delivererId })
    }

    @Test
    fun `getPendingOrders returns orders in preparation statuses`() = runTest {
        val result = repository.getPendingOrders()

        assertTrue(result.isSuccess)
        val orders = result.getOrNull() ?: emptyList()
        val validStatuses = listOf(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING
        )
        assertTrue(orders.all { it.status in validStatuses })
    }

    @Test
    fun `getAllOrders returns all orders`() = runTest {
        val result = repository.getAllOrders()

        assertTrue(result.isSuccess)
        val orders = result.getOrNull() ?: emptyList()
        assertTrue(orders.isNotEmpty())
    }
}