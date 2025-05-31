package com.example.myapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapp.R
import com.example.myapp.viewmodel.CreateOrderViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar



@Composable
fun CreateOrderScreen(
    viewModel: CreateOrderViewModel = viewModel(),
    onPay: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach(viewModel::addImage)
    }

    val formattedDateTime = uiState.selectedDateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.title_create_order),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        // Фото
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.selectedImages.forEach { uri ->
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = stringResource(R.string.cd_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.removeImage(uri) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_remove_photo)
                        )
                    }
                }
            }
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = stringResource(R.string.cd_add_photo)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Услуги
        Text(
            text = stringResource(R.string.label_services),
            style = MaterialTheme.typography.titleMedium
        )
        uiState.selectedServices.forEach { (id, selected) ->
            val label = when (id) {
                1 -> stringResource(R.string.service_jacket)
                2 -> stringResource(R.string.service_pants)
                3 -> stringResource(R.string.service_shirt)
                else -> stringResource(R.string.service_custom, id)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(selected) { viewModel.toggleService(id, it) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { viewModel.toggleService(id, it) }
                )
                Text(label, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Дата и время
        OutlinedTextField(
            value = formattedDateTime,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_date_time)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    val now = java.util.Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    viewModel.setDateTime(
                                        java.time.LocalDateTime.of(y, m + 1, d, h, min)
                                    )
                                },
                                now.get(java.util.Calendar.HOUR_OF_DAY),
                                now.get(java.util.Calendar.MINUTE),
                                true
                            ).show()
                        },
                        now.get(java.util.Calendar.YEAR),
                        now.get(java.util.Calendar.MONTH),
                        now.get(java.util.Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.cd_pick_date)
                    )
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // Адрес
        OutlinedTextField(
            value = uiState.address,
            onValueChange = viewModel::setAddress,
            label = { Text(stringResource(R.string.label_address)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Промокод
        OutlinedTextField(
            value = uiState.promoCode,
            onValueChange = { viewModel.setPromoCode(it) },
            label = { Text(stringResource(R.string.label_promo)) },
            modifier = Modifier.fillMaxWidth()
        )
        uiState.promoErrorMessage?.let { errorText ->
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Итоговая сумма
        Text(
            text = stringResource(
                R.string.text_total,
                "%,.2f".format(uiState.total)
            ),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.submitOrder { newId ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_order_id, newId),
                        Toast.LENGTH_LONG
                    ).show()
                    onPay()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedServices.filterValues { it }.isNotEmpty() &&
                    uiState.address.isNotBlank() &&
                    uiState.selectedDateTime != null &&
                    (uiState.promoCode.isBlank() || uiState.discountApplied)
        ) {
            Text(stringResource(R.string.btn_pay))
        }
    }
}
