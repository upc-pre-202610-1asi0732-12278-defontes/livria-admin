package com.example.adminlivria.common.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adminlivria.profilecontext.domain.AdminUser
import com.example.adminlivria.common.components.LivriaBottomNavBar
import com.example.adminlivria.common.components.LivriaTopBar
import com.example.adminlivria.searchcontext.presentation.HomeScreen
import com.example.adminlivria.profilecontext.presentation.SettingsScreen
import com.example.adminlivria.profilecontext.presentation.LoginScreen
import com.example.adminlivria.orderscontext.presentation.OrdersScreen
import com.example.adminlivria.stockcontext.presentation.AddBookScreen

import com.example.adminlivria.profilecontext.data.local.TokenManager
import com.example.adminlivria.common.authServiceInstance
import com.example.adminlivria.common.userAdminServiceInstance
import com.example.adminlivria.common.initializeTokenManager
import com.example.adminlivria.profilecontext.presentation.LoginViewModel
import com.example.adminlivria.profilecontext.presentation.LoginViewModelFactory
import com.example.adminlivria.profilecontext.presentation.SettingsViewModel
import com.example.adminlivria.profilecontext.presentation.SettingsViewModelFactory
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope


import com.example.adminlivria.bookcontext.presentation.BooksScreen
import com.example.adminlivria.bookcontext.presentation.BooksManagementViewModel
import com.example.adminlivria.bookcontext.presentation.BooksViewModelFactory
import com.example.adminlivria.bookcontext.presentation.detail.BookDetailScreen
import com.example.adminlivria.bookcontext.presentation.stock.StockScreen
import kotlinx.coroutines.launch
import com.example.adminlivria.orderscontext.presentation.OrdersViewModel
import com.example.adminlivria.orderscontext.presentation.OrdersViewModelFactory
import com.example.adminlivria.orderscontext.presentation.orderdetail.OrderDetailScreen
import com.example.adminlivria.searchcontext.presentation.HomeViewModelFactory
import com.example.adminlivria.statscontext.presentation.StatsScreen
import com.example.adminlivria.statscontext.presentation.StatsViewModelFactory

@Composable
fun AdminNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    initializeTokenManager(context)
    val tokenManager = TokenManager(context)
    Log.d("AdminNavGraph", "token=${tokenManager.getToken()} adminId=${tokenManager.getAdminId()}")


    val loginViewModelFactory = LoginViewModelFactory(
        authService = authServiceInstance,
        tokenManager = tokenManager
    )
    val settingsViewModelFactory = SettingsViewModelFactory(
        userAdminService = userAdminServiceInstance,
        tokenManager = tokenManager
    )

    val homeViewModelFactory = remember {
        HomeViewModelFactory(
            userAdminService = userAdminServiceInstance,
            tokenManager = tokenManager
        )
    }

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = settingsViewModelFactory
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBars = currentRoute != NavDestinations.LOGIN_ROUTE

    // Una sola carga: al iniciar sesión (token) y al cambiar de pantalla (capital puede venir del servidor).
    LaunchedEffect(tokenManager.getToken(), currentRoute) {
        if (!showBars) return@LaunchedEffect
        val token = tokenManager.getToken()
        if (!token.isNullOrBlank()) {
            settingsViewModel.loadAdminData()
        }
    }

    val booksViewModel: BooksManagementViewModel = viewModel(
        factory = BooksViewModelFactory(context)
    )

    val ordersViewModel: OrdersViewModel = viewModel(
        factory = OrdersViewModelFactory(context)
    )


    Scaffold(
        topBar = {
            if (showBars) {
                LivriaTopBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    settingsViewModel = settingsViewModel
                )
            }
        },
        bottomBar = {
            if (showBars) {
                LivriaBottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = if (tokenManager.getToken() != null)
                NavDestinations.HOME_ROUTE
            else
                NavDestinations.LOGIN_ROUTE,
            modifier = Modifier.padding(paddingValues)
        ) {


            composable(NavDestinations.LOGIN_ROUTE) {
                val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate(NavDestinations.HOME_ROUTE) {
                            popUpTo(NavDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    }
                )
            }


            composable(route = NavDestinations.HOME_ROUTE) {
                HomeScreen(
                    navController = navController,
                    userAdminService = userAdminServiceInstance,
                    tokenManager = tokenManager
                )
            }


            composable(route = NavDestinations.SETTINGS_PROFILE_ROUTE) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onLogout = {
                        settingsViewModel.logout()
                        navController.navigate(NavDestinations.LOGIN_ROUTE) {
                            popUpTo(NavDestinations.HOME_ROUTE) { inclusive = true }
                        }
                    }
                )
            }


            composable(NavDestinations.BOOKS_MANAGEMENT_ROUTE) {
                BooksScreen(
                    navController = navController,
                    viewModel = booksViewModel
                )
            }
            composable(route = NavDestinations.ORDERS_MANAGEMENT_ROUTE) {
                OrdersScreen(
                    navController = navController,
                    viewModel = ordersViewModel
                )
            }
            composable(route = NavDestinations.INVENTORY_ADD_BOOK_ROUTE) {
                AddBookScreen(navController = navController)
            }
            composable(route = NavDestinations.STATISTICS_ROUTE) {

                StatsScreen(
                    navController = navController,
                )
            }

            composable("${NavDestinations.BOOK_DETAIL_ROUTE}/{bookId}") { backStack ->
                val id = backStack.arguments?.getString("bookId")?.toIntOrNull() ?: return@composable
                BookDetailScreen(bookId = id)
            }

            composable("${NavDestinations.ORDER_DETAIL_ROUTE}/{orderid}") { backStack ->
                val id = backStack.arguments?.getString("orderid")?.toIntOrNull() ?: return@composable
                OrderDetailScreen(orderId = id)
            }

            composable("${NavDestinations.INVENTORY_INDIVIDUAL_STOCK_ROUTE}/{bookId}") { backStack ->
                val id = backStack.arguments?.getString("bookId")?.toIntOrNull() ?: return@composable
                StockScreen(
                    bookId = id,
                    settingsViewModel = settingsViewModel
                )
            }
            
            composable(route = NavDestinations.USER_CLIENT_LIST_ROUTE) {
                com.example.adminlivria.clientscontext.presentation.UserClientListScreen(
                    navController = navController
                )
            }
        }
    }
}
