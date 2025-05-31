package com.example.myapp.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.LanguageOption
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*


/**
 * ViewModel хранит текущий код выбранного языка (String), напр. "ru", "en", "es", "zh" или "" (для System).
 * При смене языка просто сохраняет новый код в SharedPreferences.
 * Пересоздание Activity производится уже из UI (SettingsScreen).
 */
class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_LANGUAGE = "selected_language"
    }

    // Храним текущий код языка, по умолчанию пустая строка = системный
    private val _languageCodeFlow = MutableStateFlow(loadSavedLanguage(application))
    val languageCodeFlow: StateFlow<String> = _languageCodeFlow

    /**
     * Возвращает из SharedPreferences сохранённый код языка, либо "" (System), если ничего нет.
     */
    private fun loadSavedLanguage(app: Application): String {
        val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "") ?: ""
    }

    /**
     * Сохраняет новый код языка в SharedPreferences и обновляет Flow.
     * При этом НЕ трогает пересоздание Activity! Это нужно делать в UI (Compose).
     */
    fun setLanguage(newCode: String) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_LANGUAGE, newCode).apply()
            _languageCodeFlow.value = newCode
        }
    }
}
