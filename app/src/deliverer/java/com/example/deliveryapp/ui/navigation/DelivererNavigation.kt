package com.example.deliveryapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.deliveryapp.DelivererOrdersScreen
import com.example.deliveryapp.auth.AuthManager

sealed class DelivererScreen(val route: String, val title: String) {
    object Deliveries : DelivererScreen("deliveries", "Deliveries")
}

@Composable
fun DelivererNavigation() {
    val navController = rememberNavController()

    //Following 2 variables are from RA3, flavors auth. prompt
    val authManager = remember { AuthManager.getInstance() }
    // Get current user ID for queries
    val userId = authManager.getCurrentUserId() ?: ""

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Face, "Deliveries") },
                    label = { Text("Deliveries") },
                    selected = true,
                    onClick = { }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = DelivererScreen.Deliveries.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(DelivererScreen.Deliveries.route) {
                DelivererOrdersScreen()
            }
        }
    }
}