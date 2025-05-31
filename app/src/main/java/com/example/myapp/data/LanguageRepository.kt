package com.example.myapp.data


import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LanguageRepository(context: Context) {
    private val TAG = "LanguageRepository"

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val languageKey = "language"

    private val _languageFlow = MutableStateFlow(loadLanguage())
    val languageFlow: StateFlow<LanguageOption> = _languageFlow



    fun setLanguage(option: LanguageOption, activity: Activity) {
        Log.d(TAG, "setLanguage saving option = $option")
        saveLanguage(option)
        _languageFlow.value = option
        activity.recreate()  // Перезапускаем активити, чтобы применить новый язык
    }


    private fun loadLanguage(): LanguageOption {
        val saved = prefs.getString(languageKey, "system")
        Log.d(TAG, "loadLanguage loaded value = $saved")
        return when (saved) {
            "ru" -> LanguageOption.RUSSIAN
            "en" -> LanguageOption.ENGLISH
            "es" -> LanguageOption.SPANISH
            "zh" -> LanguageOption.CHINESE
            else -> LanguageOption.SYSTEM
        }
    }

    private fun saveLanguage(option: LanguageOption) {
        val code = when (option) {
            LanguageOption.SYSTEM -> "system"
            LanguageOption.RUSSIAN -> "ru"
            LanguageOption.ENGLISH -> "en"
            LanguageOption.SPANISH -> "es"
            LanguageOption.CHINESE -> "zh"
        }
        prefs.edit().putString(languageKey, code).apply()
    }

}
