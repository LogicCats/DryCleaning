package com.example.myapp.data


class AnalyticsDTO {
    data class AnalyticsEventRequest(
        val userId: Long?,          // nullable, если событие не привязано к юзеру
        val eventType: String,
        val details: String?
    )

    data class AnalyticsEventResponse(
        val id: Long,
        val userId: Long?,
        val eventType: String,
        val details: String?,
        val createdAt: String
    )

}
