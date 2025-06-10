package com.example.myapp.data


class PromotionDTO {
    data class PromotionCreateRequest(
        val code: String,
        val title: String,
        val description: String? = null,
        val discountPct: Double,
        val validFrom: String? = null,
        val validTo: String? = null
    )

    data class PromotionUpdateRequest(
        val title: String?,
        val description: String?,
        val discountPct: Double?,
        val validFrom: String?,
        val validTo: String?,
        val active: Boolean?
    )

    data class PromotionResponse(
        val id: Long,
        val code: String,
        val title: String,
        val description: String?,
        val discountPct: Double,
        val active: Boolean,
        val validFrom: String?,
        val validTo: String?
    )
}
