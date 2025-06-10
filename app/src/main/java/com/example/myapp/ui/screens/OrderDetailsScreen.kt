package com.example.myapp.ui.screens

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.example.myapp.R
import com.example.myapp.navigation.Screen
import com.example.myapp.viewmodel.OrderDetailsViewModel
import com.example.myapp.data.OrderDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
    viewModel: OrderDetailsViewModel = viewModel()
) {
    Log.d("ComposeLog", "OrderDetailsScreen recomposed")
    val orderId = backStackEntry.arguments?.getString("orderId") ?: return

    val orderDetails by viewModel.orderDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetails(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    LoadingContent()
                }

                errorMessage.isNotBlank() -> {
                    ErrorContent(
                        message = errorMessage,
                        onRetry = { viewModel.fetchOrderDetails(orderId) }
                    )
                }

                orderDetails != null -> {
                    OrderDetailsContent(order = orderDetails!!)
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Log.d("ComposeLog", "LoadingContent recomposed")
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.loading_details))
    }
}

@Composable
fun GlideImage(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { imageView ->
            Glide.with(imageView.context)
                .load(url)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(imageView)
        },
        modifier = modifier
    )
}


@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Log.d("ComposeLog", "ErrorContent recomposed")
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.btn_retry))
        }
    }
}

@Composable
private fun OrderDetailsContent(order: OrderDTO.OrderDetailsResponse) {
    Log.d("ComposeLog", "OrderDetailsContent recomposed")

    val baseUrl = "http://10.0.2.2:8888"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.details_id, order.id),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.details_created, order.createdAt),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.details_address, order.address),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.details_scheduled, order.scheduledDateTime),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))



        if (order.imageUrls.isNotEmpty()) {
            Text(
                text = stringResource(R.string.details_photos_label),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            order.imageUrls.forEach { relativeUrl ->
                val fullUrl = baseUrl.trimEnd('/') + "/" + relativeUrl.trimStart('/')



                AsyncImage(
                    model = fullUrl,
                    contentDescription = stringResource(R.string.cd_order_photo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder), // добавь заглушку
                    error = painterResource(R.drawable.error_image) // добавь картинку ошибки

                )

            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        order.promoCode?.let { promo ->
            Text(
                text = stringResource(R.string.details_promo, promo),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = stringResource(R.string.details_total, "%,.2f".format(order.totalAmount)),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}


