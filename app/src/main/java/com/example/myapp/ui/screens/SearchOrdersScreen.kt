package com.example.myapp.ui.screens


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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.data.Order
import com.example.myapp.viewmodel.SearchViewModel



/**
 * @param onOrderClick Коллбэк, вызываемый при клике на любую строку результата — передаётся ID заказа.
 */
@Composable
fun SearchOrdersScreen(
    viewModel: SearchViewModel = viewModel(),
    onOrderClick: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Состояния из ViewModel
    val query by viewModel.query.collectAsState()
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.error.value
    val results = viewModel.results
    val history = viewModel.history

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
            results.isEmpty() -> {
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

            // 4) Список результатов: при клике сразу переходим на детали
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { order: Order ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Вызываем callback с ID заказа
                                    onOrderClick(order.id)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.text_order_id, order.id),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.text_order_date, order.createdAt),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = stringResource(
                                    R.string.text_order_total,
                                    "%,.2f".format(order.total)
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
