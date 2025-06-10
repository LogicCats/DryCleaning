package com.example.myapp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.ui.screens.*
import com.example.myapp.viewmodel.AuthViewModel
import com.example.myapp.viewmodel.CreateOrderViewModel
import com.example.myapp.viewmodel.SearchViewModel
import androidx.compose.ui.Modifier



// Сеалед класс с маршрутами, если ещё не описан, оставляем прежним:
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Main : Screen("main")
    object Profile : Screen("profile")
    object CreateOrder : Screen("create_order")
    object SearchOrders : Screen("search_orders")
    object OrderList : Screen("order_list")
    object OrderDetails : Screen("order_details/{orderId}") {
        fun createRoute(id: String) = "order_details/$id"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    Log.d("ComposeLog", "AppNavHost recomposed")
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // === Login ===
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClicked = {
                    navController.navigate(Screen.Registration.route)
                }
            )
        }

        // === Registration ===
        composable(Screen.Registration.route) {
            RegistrationScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // === Main ===
        composable(Screen.Main.route) {
            MainScreen(
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onCreateOrder = { navController.navigate(Screen.CreateOrder.route) },
                onSearchOrders = { navController.navigate(Screen.SearchOrders.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        // === Profile ===
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // === CreateOrder ===
        composable(Screen.CreateOrder.route) { backStackEntry ->
            val createOrderViewModel: CreateOrderViewModel =
                viewModel(backStackEntry)
            CreateOrderScreen(
                viewModel = createOrderViewModel,
                onPay = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // === SearchOrders ===
        composable(Screen.SearchOrders.route) { backStackEntry ->
            val searchViewModel: SearchViewModel = viewModel(backStackEntry)
            SearchOrdersScreen(
                viewModel = searchViewModel,
                onOrderClick = { orderId ->
                    // Навигация к деталям заказа
                    navController.navigate(Screen.OrderDetails.createRoute(orderId))
                }
            )
        }

        // === OrderList ===
        composable(Screen.OrderList.route) {
            OrderListScreen(onOrderClick = { id ->
                navController.navigate(Screen.OrderDetails.createRoute(id))
            })
        }

        // === OrderDetails ===
        composable(
            route = Screen.OrderDetails.route,
            arguments = listOf(navArgument("orderId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            OrderDetailsScreen(
                backStackEntry = backStackEntry,
                onBack = { navController.popBackStack() }
            )
        }

        // === Settings ===
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
