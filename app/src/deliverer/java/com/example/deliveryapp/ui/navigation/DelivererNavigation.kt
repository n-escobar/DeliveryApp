package com.example.deliveryapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.deliveryapp.DelivererOrdersScreen
import com.example.deliveryapp.ui.screens.*

sealed class DelivererScreen(val route: String, val title: String) {
    object Deliveries : DelivererScreen("deliveries", "Deliveries")
}

@Composable
fun DelivererNavigation() {
    val navController = rememberNavController()

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