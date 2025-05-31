package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.data.Promotion
import com.example.myapp.ui.components.PromotionItem
import com.example.myapp.viewmodel.PromotionsViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onCreateOrder: () -> Unit,
    onSearchOrders: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Получаем PromotionsViewModel, чтобы взять список акций
    val promotionsViewModel: PromotionsViewModel = viewModel()
    val promotions: List<Promotion> = promotionsViewModel.promotions

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_catalog)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_create_order)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCreateOrder()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_search_orders)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSearchOrders()
                    }
                )
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
            // Контент экрана — всё вместе в LazyColumn, чтобы можно было скроллить вниз, если акций много
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // 1) Приветствие + заглушка «Каталог»
                item {
                    Text(
                        text = stringResource(R.string.welcome_message),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.catalog_placeholder))
                    Spacer(Modifier.height(24.dp))
                }

                // 2) Секция «Акции» (если есть хотя бы одна акция)
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
