package com.example.myapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapp.data.PromotionDTO
import com.example.myapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel, который получает список текущих акций с сервера.
 * Он хранит:
 *  - isLoading: флаг загрузки,
 *  - errorMessage: текст ошибки,
 *  - promotions: список акций (PromotionDTO.PromotionResponse).
 */
class PromotionsViewModel : ViewModel() {

    // Флаг загрузки — true, когда идёт сетевой запрос
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Сообщение об ошибке — непустая строка при ошибке
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Список акций, полученный с сервера
    private val _promotions = MutableStateFlow<List<PromotionDTO.PromotionResponse>>(emptyList())
    val promotions: StateFlow<List<PromotionDTO.PromotionResponse>> = _promotions.asStateFlow()

    init {
        // При создании ViewModel сразу запрашиваем акции
        fetchPromotions()
    }

    /**
     * Выполняет сетевой запрос для получения всех активных акций.
     * С помощью ApiService.getAllPromotions().
     */
    fun fetchPromotions() {
        _isLoading.value = true
        _errorMessage.value = ""
        _promotions.value = emptyList()

        RetrofitClient.apiService.getAllPromotions()
            .enqueue(object : Callback<List<PromotionDTO.PromotionResponse>> {
                override fun onResponse(
                    call: Call<List<PromotionDTO.PromotionResponse>>,
                    response: Response<List<PromotionDTO.PromotionResponse>>
                ) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            _promotions.value = body
                        } else {
                            _errorMessage.value = "Сервер вернул пустой список акций"
                        }
                    } else {
                        // Если HTTP-код ≠ 2xx
                        val serverMsg = response.errorBody()?.string() ?: "Не удалось получить акции"
                        _errorMessage.value = "Ошибка сервера: $serverMsg"
                    }
                }

                override fun onFailure(call: Call<List<PromotionDTO.PromotionResponse>>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Сетевая ошибка: ${t.localizedMessage}"
                }
            })
    }
}
