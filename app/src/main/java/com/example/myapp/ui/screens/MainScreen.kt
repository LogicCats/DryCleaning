package com.example.myapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.ui.components.PromotionItem
import com.example.myapp.viewmodel.PromotionsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onCreateOrder: () -> Unit,
    onSearchOrders: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Log.d("ComposeLog", "MainScreen recomposed")
    // Состояние бокового меню
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Получаем PromotionsViewModel, чтобы взять состояния: promotions, isLoading, errorMessage
    val promotionsViewModel: PromotionsViewModel = viewModel()
    val promotions by promotionsViewModel.promotions.collectAsState()
    val isLoading by promotionsViewModel.isLoading.collectAsState()
    val errorMessage by promotionsViewModel.errorMessage.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))

                // Пункт "Каталог" (просто закрываем меню)
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_catalog)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )

                // Пункт "Создать заказ"
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_create_order)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCreateOrder()
                    }
                )

                // Пункт "Поиск заказов"
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_search_orders)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSearchOrders()
                    }
                )

                // Пункт "Настройки"
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSettingsClick()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_catalog)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.cd_menu)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = stringResource(R.string.cd_profile)
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = stringResource(R.string.cd_logout)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onCreateOrder) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_create_order)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = stringResource(R.string.loading_promotions))
                        }
                    }
                    errorMessage.isNotBlank() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { promotionsViewModel.fetchPromotions() }) {
                                Text(text = stringResource(R.string.btn_retry))
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = stringResource(R.string.welcome_message),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(stringResource(R.string.catalog_placeholder))
                                Spacer(Modifier.height(24.dp))
                            }

                            if (promotions.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.promotions_header),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }

                                items(promotions) { promotion ->
                                    PromotionItem(promotion = promotion)
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
