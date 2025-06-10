package com.example.myapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.OrderDTO
import com.example.myapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel для экрана деталей одного заказа.
 * Выполняет сетевой запрос getOrderDetails(orderId) и хранит полученные данные в StateFlow.
 */
class OrderDetailsViewModel : ViewModel() {

    // Состояние загрузки (true, если идёт сетевой запрос)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибки (пустая строка означает, что ошибок нет)
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Детали заказа (OrderDetailsResponse) из бэкенда
    private val _orderDetails = MutableStateFlow<OrderDTO.OrderDetailsResponse?>(null)
    val orderDetails: StateFlow<OrderDTO.OrderDetailsResponse?> = _orderDetails.asStateFlow()

    /**
     * Загружает детали заказа с сервера по его ID.
     * @param orderId строковый идентификатор заказа.
     */
    fun fetchOrderDetails(orderId: String) {
        // Сбрасываем предыдущее состояние
        _isLoading.value = true
        _errorMessage.value = ""
        _orderDetails.value = null

        RetrofitClient.apiService.getOrderDetails(orderId)
            .enqueue(object : Callback<OrderDTO.OrderDetailsResponse> {
                override fun onResponse(
                    call: Call<OrderDTO.OrderDetailsResponse>,
                    response: Response<OrderDTO.OrderDetailsResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Помещаем полученный объект в StateFlow
                            Log.d("OrderDebug", "Fetched order: ${orderDetails}")

                            _orderDetails.value = body
                        } else {
                            // Если сервер вернул пустое тело
                            Log.d("OrderDebug", "Fetched order: ${orderDetails}")

                            _errorMessage.value = "Пустой ответ от сервера"
                        }
                    } else {
                        // Сервер вернул ошибку (HTTP-код ≠ 2xx)
                        val serverMsg = response.errorBody()?.string() ?: "Не удалось получить детали заказа"
                        _errorMessage.value = "Ошибка сервера: $serverMsg"
                    }
                }

                override fun onFailure(call: Call<OrderDTO.OrderDetailsResponse>, t: Throwable) {
                    // Сетевая ошибка (например, потеря соединения)
                    _isLoading.value = false
                    _errorMessage.value = "Сетевая ошибка: ${t.localizedMessage}"
                }
            })
    }


}
