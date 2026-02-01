package com.example.deliveryapp.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.deliveryapp.ui.screens.*
import com.example.deliveryapp.viewmodel.ProductSearchViewModel
import com.example.deliveryapp.auth.AuthManager

sealed class ShopperScreen(val route: String, val title: String) {
    object CategoryBrowse : ShopperScreen("category_browse", "Browse")
    object ProductSearch : ShopperScreen("products", "Search")
    object Orders : ShopperScreen("orders", "My Orders")
    object ProductDetail : ShopperScreen("product_detail/{productId}", "Product Details") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
}

@Composable
fun ShopperNavigation() {
    val navController = rememberNavController()

    // Get activity context
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Create ViewModel scoped to activity (survives navigation)
    val productSearchViewModel: ProductSearchViewModel = viewModel(
        viewModelStoreOwner = activity ?: error("Activity not found")
    )

    //Following 2 variables are from RA3, flavors auth. prompt
    val authManager = remember { AuthManager.getInstance() }
    // Get current user ID for queries
    val userId = authManager.getCurrentUserId() ?: ""

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val showBottomBar = currentRoute !in listOf(ShopperScreen.ProductDetail.route)

            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, "Browse") },
                        label = { Text("Browse") },
                        selected = currentRoute == ShopperScreen.CategoryBrowse.route,
                        onClick = {
                            navController.navigate(ShopperScreen.CategoryBrowse.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, "Search") },
                        label = { Text("Search") },
                        selected = currentRoute == ShopperScreen.ProductSearch.route,
                        onClick = {
                            navController.navigate(ShopperScreen.ProductSearch.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, "Orders") },
                        label = { Text("Orders") },
                        selected = currentRoute == ShopperScreen.Orders.route,
                        onClick = {
                            navController.navigate(ShopperScreen.Orders.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ShopperScreen.CategoryBrowse.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ShopperScreen.CategoryBrowse.route) {
                CategoryBrowseScreen(
                    onProductClick = { productId ->
                        navController.navigate(ShopperScreen.ProductDetail.createRoute(productId))
                    },
                    onAddToCart = { product, quantity ->
                        productSearchViewModel.addToCart(product, quantity)
                    }
                )
            }

            composable(ShopperScreen.ProductSearch.route) {
                ProductSearchScreen(
                    viewModel = productSearchViewModel,
                    onProductClick = { productId ->
                        navController.navigate(ShopperScreen.ProductDetail.createRoute(productId))
                    },
                    onNavigateToOrders = {
                        navController.navigate(ShopperScreen.Orders.route) {
                            popUpTo(ShopperScreen.ProductSearch.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ShopperScreen.Orders.route) {
                ShopperOrdersScreen()
            }

            composable(
                route = ShopperScreen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
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