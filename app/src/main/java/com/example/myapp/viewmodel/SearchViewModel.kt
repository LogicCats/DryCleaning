package com.example.myapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.myapp.data.OrderDTO
import com.example.myapp.network.RetrofitClient
import com.example.myapp.util.AnalyticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel для экрана поиска заказов.
 * Теперь всё состояние — StateFlow, чтобы Compose мог использовать collectAsState().
 */
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val QUERY_KEY = "query"
        private const val MAX_HISTORY_SIZE = 10

        // Будем запрашивать с нулевой страницы, размер страницы возьмём, например, 100 заказов
        private const val PAGE_NUMBER = 0
        private const val PAGE_SIZE = 100
    }

    /** Текущий текст в строке поиска. */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")

    /** Индикатор загрузки данных из сети. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Сообщение об ошибке, если сеть вернула ошибку. */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Список результатов поиска: OrderSummaryResponse, получаем из бэкенда.
     */
    private val _results = MutableStateFlow<List<OrderDTO.OrderSummaryResponse>>(emptyList())
    val results: StateFlow<List<OrderDTO.OrderSummaryResponse>> = _results.asStateFlow()

    /**
     * История последних запросов (максимум 10). Отображается, когда строка поиска пуста.
     */
    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    init {
        // При инициализации сразу делаем пустой поиск (загрузим все заказы)
        performSearch()
    }

    /** Меняем текст поискового запроса и сохраняем его в SavedStateHandle. */
    fun onQueryChange(newQuery: String) {
        savedStateHandle[QUERY_KEY] = newQuery
    }

    /** Очищаем строку поиска, сбрасываем ошибки и показываем все заказы (первоначальный запрос). */
    fun clearQuery() {
        savedStateHandle[QUERY_KEY] = ""
        _errorMessage.value = null
        _results.value = emptyList()
        performSearch()
    }

    /**
     * Добавляем термин в историю (максимум 10 последних уникальных запросов).
     */
    private fun addToHistory(term: String) {
        if (term.isBlank()) return

        val currentList = _history.value.toMutableList()
        // Если уже был в истории, убираем, чтобы потом заново добавить в начало
        currentList.remove(term)
        currentList.add(0, term)

        if (currentList.size > MAX_HISTORY_SIZE) {
            // Убираем последний элемент по индексу, чтобы не использовать removeLast()
            currentList.removeAt(currentList.size - 1)
        }

        _history.value = currentList
    }

    /** Очищает всю историю запросов. */
    fun clearHistory() {
        _history.value = emptyList()
        AnalyticsManager.logEvent("history_cleared", "")
    }

    /**
     * Запускает поиск:
     * 1) Берём текущее значение query.
     * 2) Делаем Retrofit‐вызов searchOrders(query, PAGE_NUMBER, PAGE_SIZE).
     * 3) В onResponse заполняем список _results и историю.
     * 4) В onFailure пишем в _errorMessage.
     */
    fun performSearch() {
        val q = query.value.trim()
        _isLoading.value = true
        _errorMessage.value = null

        RetrofitClient.apiService
            .searchOrders(q, PAGE_NUMBER, PAGE_SIZE)
            .enqueue(object : Callback<OrderDTO.PageResponse<OrderDTO.OrderSummaryResponse>> {
                override fun onResponse(
                    call: Call<OrderDTO.PageResponse<OrderDTO.OrderSummaryResponse>>,
                    response: Response<OrderDTO.PageResponse<OrderDTO.OrderSummaryResponse>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val contentList = body?.content ?: emptyList()
                        _results.value = contentList

                        if (q.isNotEmpty()) {
                            addToHistory(q)
                        }

                        AnalyticsManager.logEvent(
                            "search_performed",
                            "query=$q, results_count=${contentList.size}"
                        )
                    } else {
                        val serverMsg = response.errorBody()?.string() ?: "Unknown server error"
                        _errorMessage.value = "Ошибка сервера: $serverMsg"
                        AnalyticsManager.logEvent("search_failed", "query=$q, error=$serverMsg")
                    }
                    _isLoading.value = false
                }

                override fun onFailure(
                    call: Call<OrderDTO.PageResponse<OrderDTO.OrderSummaryResponse>>,
                    t: Throwable
                ) {
                    val networkMsg = t.localizedMessage ?: "Network error"
                    _errorMessage.value = "Сетевая ошибка: $networkMsg"
                    AnalyticsManager.logEvent("search_failed", "query=$q, exception=$networkMsg")
                    _isLoading.value = false
                }
            })
    }
}
