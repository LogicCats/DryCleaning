package com.example.myapp.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.data.OrderDTO
import com.example.myapp.viewmodel.SearchViewModel

/**
 * Экран поиска заказов.
 * Подключается к SearchViewModel и отображает:
 *  1) Поле ввода запроса
 *  2) Историю запросов (если поле пустое)
 *  3) Индикатор загрузки / Ошибку / Список результатов
 *
 * @param viewModel – создаётся автоматически через viewModel()
 * @param onOrderClick – коллбэк, вызываемый при тапе по любому элементу списка; передаём сюда ID заказа.
 */
@Composable
fun SearchOrdersScreen(
    viewModel: SearchViewModel = viewModel(),
    onOrderClick: (String) -> Unit
) {

    Log.d("ComposeLog", "SearchOrdersScreen recomposed")
    val focusManager = LocalFocusManager.current

    // Собираем состояния из ViewModel
    val query by viewModel.query.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val results by viewModel.results.collectAsState()
    val history by viewModel.history.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // === Search Bar ===
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.label_search_orders)) },
            placeholder = { Text(stringResource(R.string.placeholder_enter_order)) },
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.clearQuery()
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cd_clear)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.performSearch()
                    focusManager.clearFocus()
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // === Если поле пустое и есть история ===
        if (query.isEmpty() && history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_search_history),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text(stringResource(R.string.btn_clear_history))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(history.take(5)) { term ->
                    Text(
                        text = term,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onQueryChange(term)
                                viewModel.performSearch()
                                focusManager.clearFocus()
                            }
                            .padding(vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // === Основной блок: Loading / Error / No Results (+ Retry) / Список результатов ===
        when {
            // 1) Индикатор загрузки
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // 2) Ошибка + Retry
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            viewModel.performSearch()
                            focusManager.clearFocus()
                        }) {
                            Text(stringResource(R.string.btn_retry))
                        }
                    }
                }
            }

            // 3) Нет результатов → placeholder + Retry
            !isLoading && results.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.text_no_results),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            viewModel.performSearch()
                            focusManager.clearFocus()
                        }) {
                            Text(stringResource(R.string.btn_retry))
                        }
                    }
                }
            }

            // 4) Список результатов: при клике переходим в детали
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { orderSummary: OrderDTO.OrderSummaryResponse ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOrderClick(orderSummary.id)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.text_order_id, orderSummary.id),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.text_order_date, orderSummary.createdAt),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = stringResource(
                                    R.string.text_order_total,
                                    "%,.2f".format(orderSummary.totalAmount)
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
