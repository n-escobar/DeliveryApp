package com.example.deliveryapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Shared authentication manager for both Shopper and Deliverer flavors
 * Handles all Firebase Authentication operations
 */
class AuthManager private constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = Firebase.firestore

    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Authentication state
    private val _authState = MutableStateFlow<AuthState>(
        if (auth.currentUser != null) AuthState.Authenticated
        else AuthState.Unauthenticated
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Listen to auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            _authState.value = if (firebaseAuth.currentUser != null) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    /**
     * Sign up with email and password
     * Creates user profile with role in Firestore
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: UserRole
    ): Result<FirebaseUser> {
        return try {
            // Create Firebase Auth user
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")

            // Update display name
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            ).await()

            // Create user profile in Firestore
            val userProfile = UserProfile(
                uid = user.uid,
                email = email,
                displayName = displayName,
                role = role,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userProfile)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign in failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(userId: String? = null): Result<UserProfile> {
        return try {
            val uid = userId ?: getCurrentUserId() ?: throw Exception("Not logged in")

            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val profile = document.toObject(UserProfile::class.java)
                ?: throw Exception("Profile not found")

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if current user has specific role
     */
    suspend fun hasRole(expectedRole: UserRole): Boolean {
        return try {
            val profile = getUserProfile().getOrNull()
            profile?.role == expectedRole
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Check if authenticated
     */
    fun isAuthenticated(): Boolean = auth.currentUser != null

    companion object {
        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager().also { instance = it }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// Supporting Classes
// ════════════════════════════════════════════════════════════════════════════

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

enum class UserRole {
    SHOPPER,
    DELIVERER,
    ADMIN
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.SHOPPER,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val createdAt: Long = 0,
    val fcmToken: String? = null
)