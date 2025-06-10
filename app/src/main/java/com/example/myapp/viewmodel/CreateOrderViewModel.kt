package com.example.myapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapp.R
import com.example.myapp.analytics.AnalyticsManager
import com.example.myapp.data.OrderDTO
import com.example.myapp.data.PrefsKeys
import com.example.myapp.data.PromotionDTO
import com.example.myapp.network.RetrofitClient
import com.example.myapp.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * UI‐состояние для экрана создания заказа.
 */
data class CreateOrderUiState(
    val selectedImages: List<Uri> = emptyList(),
    val selectedServices: Map<Int, Boolean> = mapOf(1 to false, 2 to false, 3 to false),
    val selectedDateTime: LocalDateTime? = null,
    val address: String = "",
    val promoCode: String = "",
    val promoErrorMessage: String? = null,
    val discountApplied: Boolean = false,
    val total: Double = 0.0
)

class CreateOrderViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Перечисляем цены на доступные услуги (идентификаторы 1,2,3)
    private val servicePrices = mapOf(1 to 500.0, 2 to 400.0, 3 to 300.0)
//    // Процент скидки при верном промокоде
//    private val promoDiscount = 0.10
//    // Набор валидных промокодов
//    private val validPromoCodes = setOf("CLEAN10", "WELCOME10", "SPRING15")

    private val promotions = mutableListOf<PromotionDTO.PromotionResponse>()


    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()



    init {
        fetchPromotions()
        recalculateTotal()
    }

    private fun fetchPromotions() {
        RetrofitClient.apiService.getAllPromotions().enqueue(object : Callback<List<PromotionDTO.PromotionResponse>> {
            override fun onResponse(
                call: Call<List<PromotionDTO.PromotionResponse>>,
                response: Response<List<PromotionDTO.PromotionResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        promotions.clear()
                        promotions.addAll(it)
                    }
                } else {
                    // Обработка ошибок (например, логгирование)
                }
            }

            override fun onFailure(call: Call<List<PromotionDTO.PromotionResponse>>, t: Throwable) {
                // Обработка ошибок (например, логгирование)
            }
        })
    }


    // Пересчитывает итоговую сумму с учётом выбранных услуг и скидки (если применена)
    private fun recalculateTotal() {
        val state = _uiState.value
        val baseTotal = state.selectedServices
            .filterValues { it }
            .keys
            .sumOf { servicePrices[it] ?: 0.0 }

        val matchedPromo = promotions.firstOrNull {
            it.code.equals(state.promoCode, ignoreCase = true)
        }

        val finalTotal = if (state.discountApplied && matchedPromo != null) {
            baseTotal * (1.0 - (matchedPromo.discountPct / 100.0))
        } else {
            baseTotal
        }

        _uiState.update { it.copy(total = finalTotal) }
    }


    // Переключатель конкретной услуги (по id) — выбран/не выбран
    fun toggleService(serviceId: Int, isSelected: Boolean) {
        _uiState.update { current ->
            val updatedServices = current.selectedServices.toMutableMap().apply {
                this[serviceId] = isSelected
            }
            current.copy(selectedServices = updatedServices)
        }
        recalculateTotal()
    }

    // Устанавливаем адрес
    fun setAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    // Устанавливаем дату и время
    fun setDateTime(dateTime: LocalDateTime) {
        _uiState.update { it.copy(selectedDateTime = dateTime) }
    }

    // Добавляем URI фотографии
    fun addImage(uri: Uri) {
        _uiState.update { current ->
            if (uri !in current.selectedImages) {
                current.copy(selectedImages = current.selectedImages + uri)
            } else current
        }
    }

    // Удаляем URI фотографии
    fun removeImage(uri: Uri) {
        _uiState.update { current ->
            current.copy(selectedImages = current.selectedImages - uri)
        }
    }

    // Устанавливаем (или валидируем) промокод
    fun setPromoCode(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    promoCode = "",
                    promoErrorMessage = null,
                    discountApplied = false
                )
            }
        } else {
            val matchedPromo = promotions.firstOrNull {
                it.code.equals(trimmed, ignoreCase = true)
            }

            if (matchedPromo != null) {
                _uiState.update {
                    it.copy(
                        promoCode = matchedPromo.code,
                        promoErrorMessage = null,
                        discountApplied = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        promoCode = trimmed,
                        promoErrorMessage = getApplication<Application>().getString(R.string.error_invalid_promo),
                        discountApplied = false
                    )
                }
            }
        }
        recalculateTotal()
    }


    // Сбрасываем введённый промокод
    fun clearPromoCode() {
        _uiState.update {
            it.copy(
                promoCode = "",
                promoErrorMessage = null,
                discountApplied = false
            )
        }
        recalculateTotal()
    }

    /**
     * Отправляет заказ на сервер:
     * 1) Формирует JSON‐строку для списка услуг без сторонних библиотек (например, "[1,2,3]").
     * 2) Упаковывает все текстовые поля (адрес, дату/время, промокод, список услуг) в RequestBody.
     * 3) Преобразует каждый Uri изображения в MultipartBody.Part.
     * 4) Вызывает RetrofitClient.apiService.createOrder(...) с .enqueue(…).
     * 5) В onResponse: при успехе извлекает ID заказа, логирует через AnalyticsManager,
     *    планирует локальное уведомление (если нужно) и вызывает onComplete(newId).
     * 6) В onFailure: логирует сетевую ошибку.
     */
    fun submitOrder(onComplete: (String) -> Unit) {
        val state = _uiState.value

        // 1) Собираем выбранные услуги, дату/время и адрес
        val chosenServices = state.selectedServices.filterValues { it }.keys.toList()
        val dateTime = state.selectedDateTime
        val addr = state.address.trim()

        // Если хотя бы одно обязательное поле не заполнено — просто выходим
        if (chosenServices.isEmpty() || addr.isBlank() || dateTime == null) {
            return
        }

        // 2) Формируем JSON‐строку вручную: "[1,2,3]"
        val servicesJsonArray = chosenServices.joinToString(
            prefix = "[",
            postfix = "]"
        ) { it.toString() }

        // 3) Подготавливаем RequestBody для каждого текстового поля
        val addressBody = addr.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateTimeBody = dateTime
            .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            .toRequestBody("text/plain".toMediaTypeOrNull())

        // Промокод передаём, только если он не пуст и discountApplied=true
        val promoBody = state.promoCode
            .takeIf { it.isNotBlank() && state.discountApplied }
            ?.toRequestBody("text/plain".toMediaTypeOrNull())

        // Список услуг как application/json (но мы уже собрали JSON-строку вручную)
        val servicesBody = servicesJsonArray.toRequestBody("application/json".toMediaTypeOrNull())

        // 4) Преобразуем каждый URI из selectedImages в MultipartBody.Part
        val parts: MutableList<MultipartBody.Part> = mutableListOf()
        state.selectedImages.forEachIndexed { index, uri ->
            try {
                // Открываем InputStream для URI
                val inputStream: InputStream? =
                    getApplication<Application>().contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes() ?: ByteArray(0)
                inputStream?.close()

                // Заворачиваем байты в RequestBody, указывая тип "image/*"
                val requestFile = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())

                // Пытаемся получить реальное имя файла через contentResolver
                val fileName: String = getFileNameFromUri(uri)
                    ?: "image_${index}.jpg"

                // Создаём MultipartBody.Part под параметр "images"
                val part = MultipartBody.Part.createFormData(
                    name = "images",
                    filename = fileName,
                    body = requestFile
                )
                parts.add(part)
            } catch (e: Exception) {
                e.printStackTrace()
                // Если не удалось обработать конкретное изображение, пропускаем его
            }
        }

        // 5) Выполняем асинхронный сетевой вызов через Retrofit
        RetrofitClient.apiService.createOrder(
            address = addressBody,
            scheduledDateTime = dateTimeBody,
            promoCode = promoBody,
            services = servicesBody,
            images = if (parts.isNotEmpty()) parts else null
        ).enqueue(object : Callback<OrderDTO.OrderDetailsResponse> {
            override fun onResponse(
                call: Call<OrderDTO.OrderDetailsResponse>,
                response: Response<OrderDTO.OrderDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // 5.1) Берём реальный ID из ответа
                        val newId = body.id
                        AnalyticsManager.logEvent(
                            "order_created",
                            "orderId=$newId, services=${chosenServices.joinToString()}, total=${state.total}"
                        )
                        // 5.2) Планируем локальное уведомление, если надо
                        scheduleNotificationIfNeeded(newId, dateTime)
                        // 5.3) Вызываем коллбэк и передаём ID
                        onComplete(newId)
                    } else {
                        // В крайне редком случае, если тело пустое, генерируем временный ID
                        val fallbackId = UUID.randomUUID().toString()
                        AnalyticsManager.logEvent(
                            "order_created_fallback_empty_body",
                            "services=${chosenServices.joinToString()}, total=${state.total}"
                        )
                        scheduleNotificationIfNeeded(fallbackId, dateTime)
                        onComplete(fallbackId)
                    }
                } else {
                    // Сервер ответил ошибкой (400, 401, 500 и т. д.)
                    val serverMsg = response.errorBody()?.string() ?: "Unknown error"
                    AnalyticsManager.logEvent(
                        "order_failed_server",
                        "error=$serverMsg"
                    )
                    // Здесь можно расширить логику, например, сохранить сообщение ошибки
                    // в отдельный StateFlow для отображения Toast/AlertDialog в Compose.
                }
            }

            override fun onFailure(call: Call<OrderDTO.OrderDetailsResponse>, t: Throwable) {
                // Сетевая ошибка (timeout, no connectivity и т.д.)
                AnalyticsManager.logEvent(
                    "order_failed_network",
                    "exception=${t.localizedMessage}"
                )
                // Тоже можно сохранить сообщение об ошибке в StateFlow для показа в UI.
            }
        })
    }

    /**
     * Вспомогательный метод: извлекаем имя файла из Uri через contentResolver.
     * Если не удалось получить, вернёт null.
     */
    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        val cursor = getApplication<Application>().contentResolver
            .query(uri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    result = c.getString(idx)
                }
            }
        }
        return result
    }

    /**
     * Планирует локальное push‐уведомление через WorkManager, если включено в настройках.
     */
    private fun scheduleNotificationIfNeeded(orderId: String, scheduledDateTime: LocalDateTime) {
        val context = getApplication<Application>()
        val prefs = context.getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean(PrefsKeys.KEY_NOTIFICATIONS_ENABLED, false)

        if (!notificationsEnabled) return

        val now = LocalDateTime.now()
        if (scheduledDateTime.isAfter(now)) {
            val duration = Duration.between(now, scheduledDateTime)
            val delayMillis = duration.toMillis()

            val inputData = Data.Builder()
                .putString(NotificationWorker.KEY_ORDER_ID, orderId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
