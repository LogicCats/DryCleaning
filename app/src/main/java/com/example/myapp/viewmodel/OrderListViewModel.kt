package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapp.data.OrderDTO
import com.example.myapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel для экрана списка заказов.
 * Загружает список через ApiService и хранит его в StateFlow.
 */
class OrderListViewModel : ViewModel() {

    // Состояние загрузки (true, если идёт сетевой запрос)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибки (пустая строка, если ошибок нет)
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Список заказов (OrderSummaryResponse) из бэкенда
    private val _orders = MutableStateFlow<List<OrderDTO.OrderSummaryResponse>>(emptyList())
    val orders: StateFlow<List<OrderDTO.OrderSummaryResponse>> = _orders.asStateFlow()

    init {
        // При создании ViewModel сразу пытаемся загрузить список заказов
        fetchOrders()
    }

    /**
     * Делает сетевой запрос для получения списка всех заказов текущего пользователя.
     * В случае успеха обновляет _orders, сбрасывает _errorMessage.
     * В случае неудачи заполняет _errorMessage.
     */
    fun fetchOrders() {
        _isLoading.value = true
        _errorMessage.value = ""

        // Асинхронный Retrofit enqueue для getAllOrders()
        RetrofitClient.apiService.getAllOrders()
            .enqueue(object : Callback<List<OrderDTO.OrderSummaryResponse>> {
                override fun onResponse(
                    call: Call<List<OrderDTO.OrderSummaryResponse>>,
                    response: Response<List<OrderDTO.OrderSummaryResponse>>

                )

                {

                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Успешно получили список → обновляем StateFlow
                            _orders.value = body
                        } else {
                            // Хотя response.isSuccessful == true, но тело null – считаем ошибкой
                            _errorMessage.value = "Пустой ответ от сервера"
                        }
                    } else {
                        // Сервер вернул код ≠ 2xx
                        val serverMsg = response.errorBody()?.string() ?: "Не удалось загрузить заказы"
                        _errorMessage.value = "Ошибка сервера: $serverMsg"
                    }
                }

                override fun onFailure(call: Call<List<OrderDTO.OrderSummaryResponse>>, t: Throwable) {
                    //Произошла сетевая ошибка
                    _isLoading.value = false
                    _errorMessage.value = "Сетевая ошибка: ${t.localizedMessage}"
                }
            })
    }
}
