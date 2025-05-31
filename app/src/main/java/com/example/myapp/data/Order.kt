package com.example.myapp.data


import java.time.LocalDateTime

data class Order(
    val id: String,
    val createdAt: LocalDateTime,
    val services: List<Int>,
    val address: String,
    val scheduledDateTime: LocalDateTime,
    val photoUris: List<String>,
    val promoCode: String?,
    val total: Double
)
