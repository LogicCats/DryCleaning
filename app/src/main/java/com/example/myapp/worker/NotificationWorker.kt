// NotificationWorker.kt
package com.example.myapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapp.R
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

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
        val orderId = inputData.getString(KEY_ORDER_ID) ?: return Result.failure()

        // Создаём канал (если нужно)
        createNotificationChannel()

        // Заголовок и текст уведомления
        val title = applicationContext.getString(R.string.notification_title)
        val text = applicationContext.getString(R.string.notification_text, orderId)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Проверяем разрешение POST_NOTIFICATIONS (требуется на Android 13+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Разрешение есть (или не нужно на версии ниже 33) → отправляем уведомление
            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID_BASE + orderId.hashCode(), builder.build())
        }
        // Иначе — пропускаем без выброса исключения

        return Result.success()
    }

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
