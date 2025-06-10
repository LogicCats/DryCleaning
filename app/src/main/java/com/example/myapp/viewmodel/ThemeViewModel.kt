package com.example.myapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.ThemeOption
import com.example.myapp.data.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для хранения и переключения темы приложения.
 * Тема сохраняется в ThemeRepository (работа с SharedPreferences).
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ThemeRepository(application)

    /**
     * Текущий выбранный вариант темы: SYSTEM / LIGHT / DARK.
     * По умолчанию — ThemeOption.SYSTEM.
     */
    val themeOption = repo.themeOptionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeOption.SYSTEM)

    /**
     * Сохраняем новый вариант темы в репозитории.
     */
    fun setTheme(option: ThemeOption) {
        viewModelScope.launch {
            repo.setThemeOption(option)
        }
    }
}
