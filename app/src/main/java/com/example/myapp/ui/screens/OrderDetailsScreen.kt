package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import coil.compose.AsyncImage
import com.example.myapp.R
import com.example.myapp.data.OrderRepository
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.myapp.data.Order




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit
) {
    val orderId = backStackEntry.arguments?.getString("orderId") ?: return
    val order: Order = OrderRepository.findById(orderId) ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.details_id, order.id),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.details_created, order.createdAt.toString()),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(R.string.details_address, order.address),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.details_scheduled,
                    order.scheduledDateTime.toString()
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            // Фотографии заказа
            order.photoUris.forEach { uriString ->
                AsyncImage(
                    model = uriString,
                    contentDescription = stringResource(R.string.cd_order_photo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Промокод (если есть)
            order.promoCode?.let { promo ->
                Text(
                    text = stringResource(R.string.details_promo, promo),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.details_total, "%,.2f".format(order.total)),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
