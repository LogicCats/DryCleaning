package com.example.myapp.ui.screens


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import java.time.format.DateTimeFormatter

@Composable
fun CreateOrderScreen(
    viewModel: CreateOrderViewModel = viewModel(),
    onPay: () -> Unit
) {
    Log.d("ComposeLog", "CreateOrderScreen recomposed")

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.addImage(uri)
        }
    }

    val formattedDateTime =
        uiState.selectedDateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: ""

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

        // 1) Блок с фотографиями
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.selectedImages.forEach { uri ->
                key(uri) {
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
            }

            // Кнопка добавить фото - ОДНА, вне цикла!
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

        // 2) Блок выбора услуг
        Text(
            text = stringResource(R.string.label_services),
            style = MaterialTheme.typography.titleMedium
        )
        uiState.selectedServices.forEach { (id, selected) ->
            key(id) {
                val label = when (id) {
                    1 -> stringResource(R.string.service_jacket)
                    2 -> stringResource(R.string.service_pants)
                    3 -> stringResource(R.string.service_shirt)
                    else -> stringResource(R.string.service_custom, id)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(selected) { checked -> viewModel.toggleService(id, checked) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = { checked -> viewModel.toggleService(id, checked) }
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3) Блок выбора даты и времени (через диалоги)
        OutlinedTextField(
            value = formattedDateTime,
            onValueChange = { /* ReadOnly */ },
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

        // 4) Поле ввода адреса
        OutlinedTextField(
            value = uiState.address,
            onValueChange = { newValue -> viewModel.setAddress(newValue) },
            label = { Text(stringResource(R.string.label_address)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 5) Поле ввода промокода
        OutlinedTextField(
            value = uiState.promoCode,
            onValueChange = { code -> viewModel.setPromoCode(code) },
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

        // 6) Итоговая сумма
        Text(
            text = stringResource(
                R.string.text_total,
                "%,.2f".format(uiState.total)
            ),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // 7) Кнопка «Оплатить» или «Отправить заказ»
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
