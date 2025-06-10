package com.example.myapp.data



class OrderDTO {
    data class OrderCreateRequest(
        val address: String,
        val scheduledDateTime: String, // ISO-8601 строка
        val promoCode: String? = null,
        val services: List<Int>
        // изображения в запросах Retrofit передаются отдельно через MultipartBody.Part, поэтому тут их нет
    )

    data class OrderUpdateRequest(
        val address: String?,
        val scheduledDateTime: String?,
        val promoCode: String?,
        val services: List<Int>?
    )

    data class OrderSummaryResponse(
        val id: String,
        val createdAt: String,
        val scheduledDateTime: String,
        val totalAmount: Double,
        val status: String
    )

    data class OrderDetailsResponse(
        val id: String,
        val userId: Long,
        val createdAt: String,
        val scheduledDateTime: String,
        val address: String,
        val promoCode: String?,
        val totalAmount: Double,
        val status: String,
        val services: List<Int>,
        val imageUrls: List<String>
    )

    data class PageResponse<T>(
        val content: List<T>,
        val totalPages: Int,
        val totalElements: Int,
        val number: Int,
        val size: Int
    )
}
