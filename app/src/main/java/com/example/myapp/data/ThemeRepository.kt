package com.example.myapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

class ThemeRepository(private val context: Context) {
    private val KEY = stringPreferencesKey("theme_option")

    /** Поток выбранной темы */
    val themeOptionFlow = context.settingsDataStore.data
        .map { prefs -> prefs[KEY]?.let(ThemeOption::valueOf) ?: ThemeOption.SYSTEM }

    suspend fun setThemeOption(option: ThemeOption) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY] = option.name
        }
    }
}
