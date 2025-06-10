package com.example.myapp.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapp.R

/**
 * Worker, который по расписанию отправляет локальное push-уведомление о запланированном заказе.
 *
 * Ожидает в inputData ключ KEY_ORDER_ID со значением ID заказа (String).
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val KEY_ORDER_ID = "order_id"
        const val CHANNEL_ID = "order_notifications_channel"
        const val NOTIFICATION_ID_BASE = 1000
    }

    override fun doWork(): Result {
        // Получаем ID заказа из входных данных
        val orderId = inputData.getString(KEY_ORDER_ID) ?: return Result.failure()

        // Убедимся, что канал уведомлений создан
        createNotificationChannel()

        // Заголовок и текст уведомления
        val title = applicationContext.getString(R.string.notification_title)
        val text = applicationContext.getString(R.string.notification_text, orderId)

        // Построим NotificationCompat.Builder
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)      // Убедитесь, что у вас есть этот ресурс
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Проверяем разрешение POST_NOTIFICATIONS (API 33+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Если либо версия ниже 33, либо разрешение уже выдано — отправляем уведомление
            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID_BASE + orderId.hashCode(), builder.build())
        }
        // Иначе — пропускаем без бросания исключения, т.к. пользователь не дал разрешение

        return Result.success()
    }

    /**
     * Создаёт канал уведомлений для заказов, если он ещё не создан
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.notification_channel_name)
            val descriptionText = applicationContext.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
