package com.example.deliveryapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.deliveryapp.auth.AuthManager
import com.example.deliveryapp.auth.AuthState
import com.example.deliveryapp.auth.UserRole
import com.example.deliveryapp.ui.navigation.DelivererNavigation
import com.example.deliveryapp.ui.screens.LoginScreen
import com.example.deliveryapp.ui.screens.SignUpScreen
import com.example.deliveryapp.ui.theme.DeliveryAppTheme
import com.google.firebase.messaging.FirebaseMessaging

class DelivererActivity : ComponentActivity() {

    private val authManager = AuthManager.getInstance()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getFcmToken()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            getFcmToken()
        }

        setContent {
            DeliveryAppTheme {
                DelivererAuthFlow()
            }
        }
    }

    @Composable
    fun DelivererAuthFlow() {
        val authState by authManager.authState.collectAsState()
        var showSignUp by remember { mutableStateOf(false) }

        when {
            authState is AuthState.Authenticated -> {
                // User is logged in - show main app
                DelivererNavigation()
            }
            showSignUp -> {
                // Show sign up screen
                SignUpScreen(
                    userRole = UserRole.DELIVERER,
                    onSignUpSuccess = {
                        showSignUp = false
                    },
                    onNavigateBack = {
                        showSignUp = false
                    }
                )
            }
            else -> {
                // Show login screen
                LoginScreen(
                    userRole = UserRole.DELIVERER,
                    onLoginSuccess = {
                        // Auth state will update automatically
                    },
                    onNavigateToSignUp = {
                        showSignUp = true
                    }
                )
            }
        }
    }

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Save token via AuthManager
            }
        }
    }
}