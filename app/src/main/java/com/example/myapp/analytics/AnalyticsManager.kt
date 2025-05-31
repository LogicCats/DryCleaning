package com.example.myapp.analytics


import android.content.Context
import android.util.Log
import com.example.myapp.data.PrefsKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * AnalyticsManager — singleton, который пишет события в локальный файл analytics.log.
 * Если флаг в SharedPreferences (KEY_ANALYTICS_ENABLED) = true, то записывает,
 * иначе игнорирует все вызовы logEvent(...).
 */
object AnalyticsManager {

    private const val TAG = "AnalyticsManager"
    private const val FILE_NAME = "analytics.log"
    private var initialized = false
    private lateinit var analyticsFile: File
    private lateinit var appContext: Context

    /**
     * Инициализация должна быть вызвана один раз из MainActivity (или другого места),
     * чтобы задать контекст и путь к файлу.
     */
    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        analyticsFile = File(appContext.filesDir, FILE_NAME)
        if (!analyticsFile.exists()) {
            try {
                analyticsFile.createNewFile()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create analytics file", e)
            }
        }
        initialized = true
    }

    /**
     * Проверяет в SharedPreferences, включена ли аналитика.
     */
    private fun isAnalyticsEnabled(): Boolean {
        val prefs = appContext.getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PrefsKeys.KEY_ANALYTICS_ENABLED, false)
    }

    /**
     * Записывает одно событие: timestamp, eventType, details
     *
     * Пример строки:
     * 2025-05-31T20:17:00 | order_created | orderId=abc123
     */
    fun logEvent(eventType: String, details: String = "") {
        if (!initialized) {
            Log.w(TAG, "AnalyticsManager not initialized. Call init() first.")
            return
        }
        if (!isAnalyticsEnabled()) {
            // Аналитика отключена — ничего не делаем
            return
        }

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val line = buildString {
            append(timestamp)
            append(" | ")
            append(eventType)
            if (details.isNotBlank()) {
                append(" | ")
                append(details)
            }
        }

        // Запись в файл в фоновом потоке (корутиной)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FileWriter(analyticsFile, /* append = */ true).use { writer ->
                    writer.appendLine(line)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write analytics event", e)
            }
        }
    }
}
