package com.example.myapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.OrderDTO
import com.example.myapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel для экрана списка заказов.
 * Загружает список через ApiService и хранит его в StateFlow.
 */
class OrderListViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderDTO.OrderSummaryResponse>>(emptyList())
    val orders = _orders.asStateFlow()

    init {
        Log.d("OrderListViewModel", "Init called")

        viewModelScope.launch {
            _searchQuery
                .onEach { Log.d("OrderListViewModel", "onEach: $it") }
                .debounce(2000)
                .onEach { Log.d("OrderListViewModel", "After debounce: $it") }
                .distinctUntilChanged()
                .collect { query ->
                    Log.d("OrderListViewModel", "Collect query: '$query'")
                    fetchOrders(query)
                }
        }

    }


    fun onSearchQueryChanged(query: String) {
        Log.d("OrderListViewModel", "onSearchQueryChanged: $query")
        _searchQuery.value = query
    }


    fun fetchOrders(query: String) {
        Log.d("OrderListViewModel", "Fetching orders with query: '$query'")
        _isLoading.value = true
        _errorMessage.value = ""

        RetrofitClient.apiService.getAllOrders()
            .enqueue(object : Callback<List<OrderDTO.OrderSummaryResponse>> {
                override fun onResponse(
                    call: Call<List<OrderDTO.OrderSummaryResponse>>,
                    response: Response<List<OrderDTO.OrderSummaryResponse>>
                ) {
                    Log.d("OrderListViewModel", "onResponse: isSuccessful=${response.isSuccessful}")
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("OrderListViewModel", "Response body size: ${body?.size ?: "null"}")
                        if (body != null) {
                            val filtered = if (query.isBlank()) body else body.filter { it.id.contains(query, ignoreCase = true) }
                            Log.d("OrderListViewModel", "Filtered orders count: ${filtered.size}")
                            _orders.value = filtered
                        } else {
                            _errorMessage.value = "Пустой ответ от сервера"
                        }
                    } else {
                        val serverMsg = response.errorBody()?.string() ?: "Не удалось загрузить заказы"
                        Log.d("OrderListViewModel", "Server error: $serverMsg")
                        _errorMessage.value = "Ошибка сервера: $serverMsg"
                    }
                }

                override fun onFailure(call: Call<List<OrderDTO.OrderSummaryResponse>>, t: Throwable) {
                    Log.d("OrderListViewModel", "onFailure: ${t.localizedMessage}")
                    _isLoading.value = false
                    _errorMessage.value = "Сетевая ошибка: ${t.localizedMessage}"
                }
            })
    }
}
