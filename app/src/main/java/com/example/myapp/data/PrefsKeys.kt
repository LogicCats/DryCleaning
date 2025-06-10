package com.example.myapp.data

object PrefsKeys {
    const val PREFS_NAME = "app_prefs"

    // Ключ для хранения кода выбранного языка (например, "ru", "en", "es", "zh", или "" для системного)
    // Мы создаём алиас: KEY_LANGUAGE → KEY_SELECTED_LANGUAGE
    const val KEY_SELECTED_LANGUAGE = "selected_language"
    const val KEY_LANGUAGE = KEY_SELECTED_LANGUAGE

    // Ключ для хранения темы (ThemeOption), если вы это используете
    const val KEY_THEME = "selected_theme"

    // Ключ для флага Push-уведомлений в настройках
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    // Ключ для флага сбора аналитики
    const val KEY_ANALYTICS_ENABLED = "analytics_enabled"

    // Новый ключ для хранения JWT-токена после логина/регистрации
    const val KEY_AUTH_TOKEN = "auth_token"
}
