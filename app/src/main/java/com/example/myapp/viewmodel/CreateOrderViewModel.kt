package com.example.myapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.myapp.data.Order
import com.example.myapp.data.OrderRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.UUID
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.myapp.data.PrefsKeys
import com.example.myapp.worker.NotificationWorker
import java.time.Duration
import com.example.myapp.analytics.AnalyticsManager


/**
 * Состояние UI для экрана создания заказа.
 * Добавлены поля для индикации ошибки промокода и информации о том, применена ли скидка.
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

    private val servicePrices = mapOf(1 to 500.0, 2 to 400.0, 3 to 300.0)
    private val promoDiscount = 0.10
    private val validPromoCodes = setOf("CLEAN10", "WELCOME10", "SPRING15")

    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState

    init {
        recalculateTotal()
    }

    private fun recalculateTotal() {
        val state = _uiState.value
        val baseTotal = state.selectedServices
            .filterValues { it }
            .keys
            .sumOf { servicePrices[it] ?: 0.0 }

        val finalTotal = if (state.discountApplied) {
            baseTotal * (1.0 - promoDiscount)
        } else {
            baseTotal
        }
        _uiState.update { it.copy(total = finalTotal) }
    }

    fun toggleService(serviceId: Int, isSelected: Boolean) {
        _uiState.update { current ->
            val updatedServices = current.selectedServices.toMutableMap().apply {
                this[serviceId] = isSelected
            }
            current.copy(selectedServices = updatedServices)
        }
        recalculateTotal()
    }

    fun setAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun setDateTime(dateTime: LocalDateTime) {
        _uiState.update { it.copy(selectedDateTime = dateTime) }
    }

    fun addImage(uri: Uri) {
        _uiState.update { current ->
            if (uri !in current.selectedImages) {
                current.copy(selectedImages = current.selectedImages + uri)
            } else current
        }
    }

    fun removeImage(uri: Uri) {
        _uiState.update { current ->
            current.copy(selectedImages = current.selectedImages - uri)
        }
    }

    fun setPromoCode(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(promoCode = "", promoErrorMessage = null, discountApplied = false) }
        } else {
            val isValid = validPromoCodes.contains(trimmed.uppercase())
            if (isValid) {
                _uiState.update {
                    it.copy(
                        promoCode = trimmed.uppercase(),
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

    fun clearPromoCode() {
        _uiState.update { it.copy(promoCode = "", promoErrorMessage = null, discountApplied = false) }
        recalculateTotal()
    }

    fun submitOrder(onComplete: (String) -> Unit) {
        val state = _uiState.value
        val chosenServices = state.selectedServices.filterValues { it }.keys.toList()
        val dateTime = state.selectedDateTime
        val addr = state.address.trim()

        if (chosenServices.isEmpty() || addr.isBlank() || dateTime == null) {
            return
        }

        val promo = state.promoCode.takeIf { it.isNotBlank() && state.discountApplied }
        val photos = state.selectedImages.map(Uri::toString)
        val newId = UUID.randomUUID().toString()

        val order = Order(
            id = newId,
            createdAt = LocalDateTime.now(),
            services = chosenServices,
            address = addr,
            scheduledDateTime = dateTime,
            photoUris = photos,
            promoCode = promo,
            total = state.total
        )

        // Сохраняем заказ
        OrderRepository.add(order)

        // Логируем факт создания заказа
        AnalyticsManager.logEvent(
            "order_created",
            "orderId=$newId, services=${chosenServices.joinToString()}, total=${state.total}"
        )

        // Планируем уведомление, если включено
        scheduleNotificationIfNeeded(newId, dateTime)

        onComplete(newId)
    }

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

            WorkManager.getInstance(getApplication()).enqueue(workRequest)
        }
    }
}
