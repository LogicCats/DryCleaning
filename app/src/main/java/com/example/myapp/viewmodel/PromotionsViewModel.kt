package com.example.myapp.viewmodel


import androidx.lifecycle.ViewModel
import com.example.myapp.data.Promotion

/**
 * ViewModel, который содержит список текущих акций.
 * В данном примере мы просто храним статический список, но при желании
 * можно подтягивать их с сервера, из базы данных и т.п.
 */
class PromotionsViewModel : ViewModel() {

    /**
     * Список текущих акций. В реальной апликухе сюда может прийти запрос
     * к API или чтение из локального репозитория.
     */
    val promotions: List<Promotion> = listOf(
        Promotion(
            title = "Скидка 10% на первое обращение",
            description = "При использовании промокода получите 10% скидки на любую услугу.",
            promoCode = "WELCOME10"
        ),
        Promotion(
            title = "Весенняя акция 15%",
            description = "Скидка 15% на химчистку пальто и костюмов.",
            promoCode = "SPRING15"
        ),
        Promotion(
            title = "Праздничная скидка 20%",
            description = "В честь праздника — 20% скидки на все услуги.",
            promoCode = "HOLIDAY20"
        )
        // При необходимости добавьте ещё акции сюда
    )
}
