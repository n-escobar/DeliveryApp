package com.example.deliveryapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.deliveryapp.DelivererOrdersScreen
import com.example.deliveryapp.ui.screens.*
import com.example.deliveryapp.viewmodel.ProductSearchViewModel

sealed class Screen(val route: String, val title: String) {
    object ProductSearch : Screen("products", "Products")
    object ShopperOrders : Screen("shopper_orders", "My Orders")
    object DelivererOrders : Screen("deliverer_orders", "Deliveries")
    object ProductDetail : Screen("product_detail/{productId}", "Product Details") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
}

@Composable
fun AppNavigation(userRole: String = "SHOPPER") {
    val navController = rememberNavController()
    // Shared ViewModel for cart management across screens
    val productSearchViewModel: ProductSearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    Scaffold(
        bottomBar = {
            // Only show bottom bar on main screens, not on detail screen
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val showBottomBar = currentRoute !in listOf(
                Screen.ProductDetail.route
            )

            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    userRole = userRole
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (userRole == "SHOPPER") {
                Screen.ProductSearch.route
            } else {
                Screen.DelivererOrders.route
            },
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.ProductSearch.route) {
                ProductSearchScreen(
                    viewModel = productSearchViewModel,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }

            composable(Screen.ShopperOrders.route) {
                ShopperOrdersScreen()
            }

            composable(Screen.DelivererOrders.route) {
                DelivererOrdersScreen()
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onAddToCart = { product, quantity ->
                        productSearchViewModel.addToCart(product, quantity)
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    userRole: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        if (userRole == "SHOPPER") {
            NavigationBarItem(
                icon = { Icon(Icons.Default.ShoppingCart, "Products") },
                label = { Text("Products") },
                selected = currentRoute == Screen.ProductSearch.route,
                onClick = {
                    navController.navigate(Screen.ProductSearch.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, "Orders") },
                label = { Text("My Orders") },
                selected = currentRoute == Screen.ShopperOrders.route,
                onClick = {
                    navController.navigate(Screen.ShopperOrders.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        } else {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Face, "Deliveries") },
                label = { Text("Deliveries") },
                selected = currentRoute == Screen.DelivererOrders.route,
                onClick = {
                    navController.navigate(Screen.DelivererOrders.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}