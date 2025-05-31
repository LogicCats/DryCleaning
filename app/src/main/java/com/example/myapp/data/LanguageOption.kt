package com.example.myapp.data

import java.util.*

enum class LanguageOption(val locale: Locale) {
    SYSTEM(Locale.getDefault()),
    RUSSIAN(Locale("ru")),
    ENGLISH(Locale("en")),
    SPANISH(Locale("es")),
    CHINESE(Locale.SIMPLIFIED_CHINESE);

    companion object {
        fun fromLocale(locale: Locale): LanguageOption {
            return entries.find { it.locale.language == locale.language } ?: SYSTEM
        }
    }
}
