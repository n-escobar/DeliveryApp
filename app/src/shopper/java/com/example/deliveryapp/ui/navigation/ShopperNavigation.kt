package com.example.deliveryapp.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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

sealed class ShopperScreen(val route: String, val title: String) {
    object ProductSearch : ShopperScreen("products", "Products")
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

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val showBottomBar = currentRoute !in listOf(ShopperScreen.ProductDetail.route)

            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, "Products") },
                        label = { Text("Products") },
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
            startDestination = ShopperScreen.ProductSearch.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ShopperScreen.ProductSearch.route) {
                ProductSearchScreen(
                    viewModel = productSearchViewModel,  // Pass the same instance
                    onProductClick = { productId ->
                        navController.navigate(ShopperScreen.ProductDetail.createRoute(productId))
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
                        productSearchViewModel.addToCart(product, quantity)  // Uses same instance
                    }
                )
            }
        }
    }
}