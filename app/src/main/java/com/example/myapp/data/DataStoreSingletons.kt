package com.example.myapp.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * Один-единственный DataStore для всего приложения, файл настроек — "settings"
 */
val Context.settingsDataStore by preferencesDataStore(name = "settings")
