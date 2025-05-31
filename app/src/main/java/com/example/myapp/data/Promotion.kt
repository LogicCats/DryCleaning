package com.example.myapp.data


/**
 * Модель для описания одной акции/промо-акции с промокодом
 *
 * @param title       Название акции (короткое)
 * @param description Дополнительное описание (необязательно, можно просто пояснить условия)
 * @param promoCode   Промокод (строка, которую пользователь вводит при оформлении заказа)
 */
data class Promotion(
    val title: String,
    val description: String,
    val promoCode: String
)
