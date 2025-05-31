package com.example.myapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.ThemeOption
import com.example.myapp.data.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ThemeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ThemeRepository(app)

    val themeOption = repo.themeOptionFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, ThemeOption.SYSTEM)

    fun setTheme(option: ThemeOption) {
        viewModelScope.launch {
            repo.setThemeOption(option)
        }
    }
}
