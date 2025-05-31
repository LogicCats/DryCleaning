// LocaleHelper.kt
package com.example.myapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale


object LocaleHelper {
    /**
     * Возвращает новый Context, у которого в Configuration установлена локаль [languageCode].
     * Если languageCode == "" или "system", значит — системный язык.
     */
    @SuppressLint("ObsoleteSdkInt")
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode.isBlank() || languageCode == "system") {
            // Возвращаемся к системной локали устройства
            Locale.getDefault()
        } else {
            Locale(languageCode)
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}

