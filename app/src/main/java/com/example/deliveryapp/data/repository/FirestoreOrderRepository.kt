package com.example.deliveryapp.data.repository

import com.example.deliveryapp.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

/**
 * Firestore implementation of OrderRepository
 * Replaces the in-memory implementation with cloud persistence
 */
class FirestoreOrderRepository : OrderRepositoryInterface {

    private val firestore: FirebaseFirestore = Firebase.firestore

    // Collection references
    private val ordersCollection = firestore.collection("orders")
    private val productsCollection = firestore.collection("products")

    // ═══════════════════════════════════════════════════════
    // ORDER OPERATIONS
    // ═══════════════════════════════════════════════════════

    override suspend fun createOrder(order: Order): Result<Order> {
        return try {
            // Convert Order to Map for Firestore
            val orderData = order.toFirestoreMap()

            // Add to Firestore
            ordersCollection.document(order.orderId).set(orderData).await()

            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val document = ordersCollection.document(orderId).get().await()

            if (document.exists()) {
                val order = document.toOrder()
                Result.success(order)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } as Result<Order>
    }

    override suspend fun getOrdersForShopper(shopperId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("shopperId", shopperId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { it.toOrder() }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrdersForDeliverer(delivererId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("delivererId", delivererId)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { it.toOrder() }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAvailableOrdersForDelivery(): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("status", OrderStatus.READY_FOR_PICKUP.name)
                .whereEqualTo("delivererId", null)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { it.toOrder() }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus
    ): Result<Order> {
        return try {
            ordersCollection.document(orderId)
                .update("status", newStatus.name)
                .await()

            getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun assignDeliverer(
        orderId: String,
        delivererId: String
    ): Result<Order> {
        return try {
            ordersCollection.document(orderId)
                .update(
                    mapOf(
                        "delivererId" to delivererId,
                        "status" to OrderStatus.OUT_FOR_DELIVERY.name
                    )
                )
                .await()

            getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════════════
    // REAL-TIME LISTENERS (FLOWS)
    // ═══════════════════════════════════════════════════════

    /**
     * Listen to shopper's orders in real-time
     */
    fun observeOrdersForShopper(shopperId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("shopperId", shopperId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { it.toOrder() } ?: emptyList()
                trySend(orders)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Listen to available orders for delivery in real-time
     */
    fun observeAvailableOrders(): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("status", OrderStatus.READY_FOR_PICKUP.name)
            .whereEqualTo("delivererId", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { it.toOrder() } ?: emptyList()
                trySend(orders)
            }

        awaitClose { listener.remove() }
    }

    // ═══════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ═══════════════════════════════════════════════════════

    private fun Order.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "orderId" to orderId,
            "shopperId" to shopperId,
            "delivererId" to delivererId,
            "items" to items.map { it.toMap() },
            "status" to status.name,
            "totalPrice" to totalPrice,
            "deliveryAddress" to deliveryAddress,
            "createdAt" to createdAt.toEpochMilli()
        )
    }

    private fun OrderItem.toMap(): Map<String, Any> {
        return mapOf(
            "productId" to productId,
            "productName" to productName,
            "priceAtPurchase" to priceAtPurchase,
            "quantity" to quantity
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOrder(): Order? {
        return try {
            Order(
                orderId = getString("orderId") ?: return null,
                shopperId = getString("shopperId") ?: return null,
                delivererId = getString("delivererId"),
                items = (get("items") as? List<Map<String, Any>>)?.map { itemMap ->
                    OrderItem(
                        productId = itemMap["productId"] as? String ?: "",
                        productName = itemMap["productName"] as? String ?: "",
                        priceAtPurchase = (itemMap["priceAtPurchase"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0
                    )
                } ?: emptyList(),
                status = OrderStatus.valueOf(getString("status") ?: "PENDING"),
                totalPrice = getDouble("totalPrice") ?: 0.0,
                deliveryAddress = getString("deliveryAddress") ?: "",
                createdAt = Instant.ofEpochMilli(getLong("createdAt") ?: 0)
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        @Volatile
        private var instance: FirestoreOrderRepository? = null

        fun getInstance(): FirestoreOrderRepository {
            return instance ?: synchronized(this) {
                instance ?: FirestoreOrderRepository().also { instance = it }
            }
        }
    }
}

// Interface for repository (enables testing and swapping implementations)
interface OrderRepositoryInterface {
    suspend fun createOrder(order: Order): Result<Order>
    suspend fun getOrderById(orderId: String): Result<Order>
    suspend fun getOrdersForShopper(shopperId: String): Result<List<Order>>
    suspend fun getOrdersForDeliverer(delivererId: String): Result<List<Order>>
    suspend fun getAvailableOrdersForDelivery(): Result<List<Order>>
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Order>
    suspend fun assignDeliverer(orderId: String, delivererId: String): Result<Order>
}