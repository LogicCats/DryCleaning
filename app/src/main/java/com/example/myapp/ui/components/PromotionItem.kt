package com.example.myapp.ui.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.PromotionDTO

/**
 * Компонент, отображающий одну акцию, принимая dto типа PromotionDTO.PromotionResponse.
 *
 * Поля PromotionResponse:
 *   - id: Long
 *   - code: String
 *   - title: String
 *   - description: String?
 *   - discountPct: Double
 *   - active: Boolean
 *   - validFrom: String?
 *   - validTo: String?
 */
@Composable
fun PromotionItem(
    promotion: PromotionDTO.PromotionResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок акции
            Text(
                text = promotion.title,
                style = MaterialTheme.typography.titleMedium
            )

            // Промокод
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Код: ${promotion.code}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Описание (если есть)
            promotion.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Скидка (процент)
            Text(
                text = "Скидка: ${"%.0f".format(promotion.discountPct)}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Статус активности
            val statusText = if (promotion.active) {
                "Активна"
            } else {
                "Неактивна"
            }
            Text(
                text = "Статус: $statusText",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Период действия
            val from = promotion.validFrom
            val to = promotion.validTo
            if (from != null && to != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Действует с $from по $to",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (from != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Действует с $from",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (to != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Действует до $to",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
