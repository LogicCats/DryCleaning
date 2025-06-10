package com.example.myapp.ui.screens

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.data.PrefsKeys
import com.example.myapp.data.ThemeOption
import com.example.myapp.viewmodel.LanguageViewModel
import com.example.myapp.viewmodel.ThemeViewModel
import java.util.Locale

/**
 * Экран «Настройки»:
 *  – выбор темы (System / Light / Dark)
 *  – выбор языка (system/ru/en/es/zh)
 *  – чекбоксы Push‐уведомлений и Analytics
 *  – вывод версии и ссылку на Privacy Policy
 *
 * ThemeViewModel и LanguageViewModel работают через SharedPreferences, сетевых вызовов здесь не требуется.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    Log.d("ComposeLog", "SettingsScreen recomposed")
    // Получаем ViewModel'ы
    val themeVm: ThemeViewModel = viewModel()
    val langVm: LanguageViewModel = viewModel()

    // Состояния из потоков ViewModel
    val currentTheme by themeVm.themeOption.collectAsState()
    val currentLangCode by langVm.languageCodeFlow.collectAsState()

    val context = LocalContext.current
    val activity = (context as? Activity)

    // SharedPreferences
    val prefs: SharedPreferences =
        context.getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)

    // Чекбокс Push‐уведомлений
    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean(PrefsKeys.KEY_NOTIFICATIONS_ENABLED, false))
    }

    // Чекбокс Analytics
    var analyticsEnabled by remember {
        mutableStateOf(prefs.getBoolean(PrefsKeys.KEY_ANALYTICS_ENABLED, false))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.options)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ===== App Theme =====
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ThemeOption.entries.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == currentTheme,
                        onClick = { themeVm.setTheme(option) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (option) {
                            ThemeOption.SYSTEM -> stringResource(R.string.theme_system)
                            ThemeOption.LIGHT -> stringResource(R.string.theme_light)
                            ThemeOption.DARK -> stringResource(R.string.theme_dark)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== Language =====
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val entries = listOf(
                "" to stringResource(R.string.language_system, Locale.getDefault().displayLanguage),
                "ru" to stringResource(R.string.language_russian),
                "en" to stringResource(R.string.language_english),
                "es" to stringResource(R.string.language_spanish),
                "zh" to stringResource(R.string.language_chinese)
            )
            entries.forEach { (langCode, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (langCode == currentLangCode),
                        onClick = {
                            langVm.setLanguage(langCode)
                            // Пересоздаем Activity, чтобы язык применился сразу
                            activity?.recreate()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = label)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== Push notifications =====
            Text(stringResource(R.string.advanced), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = notificationsEnabled,
                    onCheckedChange = { isChecked ->
                        notificationsEnabled = isChecked
                        prefs.edit()
                            .putBoolean(PrefsKeys.KEY_NOTIFICATIONS_ENABLED, isChecked)
                            .apply()
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.push_notifications))
            }

            Spacer(Modifier.height(16.dp))

            // ===== Analytics collection =====
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = analyticsEnabled,
                    onCheckedChange = { isChecked ->
                        analyticsEnabled = isChecked
                        prefs.edit()
                            .putBoolean(PrefsKeys.KEY_ANALYTICS_ENABLED, isChecked)
                            .apply()
                        // AnalyticsManager сам проверяет флаг SharedPreferences перед записью
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.analytics_collection))
            }

            Spacer(Modifier.height(24.dp))

            // ===== About app =====
            Text(stringResource(R.string.about_app), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.version, "1.0.0"))
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { /* TODO: открыть Privacy Policy */ }) {
                Text(stringResource(R.string.privacy_policy))
            }
        }
    }
}
