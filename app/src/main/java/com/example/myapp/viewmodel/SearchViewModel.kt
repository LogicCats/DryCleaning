package com.example.myapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.Order
import com.example.myapp.data.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.myapp.analytics.AnalyticsManager

class SearchViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val QUERY_KEY = "query"
        private const val MAX_HISTORY_SIZE = 10
    }

    val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val results = mutableStateListOf<Order>()
    val history = mutableStateListOf<String>()

    init {
        // Инициализируем результаты
        results.clear()
        results.addAll(OrderRepository.getAll())
        performSearch()
    }

    fun onQueryChange(newQuery: String) {
        savedStateHandle[QUERY_KEY] = newQuery
    }

    fun clearQuery() {
        savedStateHandle[QUERY_KEY] = ""
        error.value = null
        results.clear()
        results.addAll(OrderRepository.getAll())
    }

    fun performSearch() {
        val q = query.value.trim()
        isLoading.value = true
        error.value = null

        viewModelScope.launch(Dispatchers.Main) {
            delay(200) // эмуляция задержки

            try {
                val allOrders = OrderRepository.getAll()
                val filtered = if (q.isEmpty()) {
                    allOrders
                } else {
                    allOrders.filter { it.id.contains(q, ignoreCase = true) }
                }

                results.clear()
                results.addAll(filtered)

                if (q.isNotEmpty()) {
                    history.remove(q)
                    history.add(0, q)
                    if (history.size > MAX_HISTORY_SIZE) {
                        history.removeAt(history.size - 1)
                    }
                }

                // Логируем факт выполнения поиска (или пустого запроса, чтобы тоже учесть)
                AnalyticsManager.logEvent(
                    "search_performed",
                    "query=$q, results_count=${filtered.size}"
                )
            } catch (e: Exception) {
                error.value = e.message ?: "Unknown error"
                AnalyticsManager.logEvent("search_failed", "query=$q,error=${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun clearHistory() {
        history.clear()
        AnalyticsManager.logEvent("history_cleared", "")
    }

    fun retry() {
        performSearch()
    }
}
