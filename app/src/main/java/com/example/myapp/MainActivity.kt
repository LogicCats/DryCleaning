package com.example.myapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.core.content.ContextCompat
import com.example.myapp.analytics.AnalyticsManager
import com.example.myapp.data.PrefsKeys
import com.example.myapp.data.ThemeOption
import com.example.myapp.navigation.AppNavHost
import com.example.myapp.ui.theme.CleaningAppTheme
import com.example.myapp.util.LocaleHelper
import com.example.myapp.viewmodel.LanguageViewModel
import com.example.myapp.viewmodel.ThemeViewModel
import com.example.myapp.network.RetrofitClient

class MainActivity : ComponentActivity() {

    // Получаем ViewModel-ы для языка и темы
    private val languageViewModel: LanguageViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    // Launcher для запроса разрешения POST_NOTIFICATIONS (Android 13+)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Здесь при желании можно обработать результат разрешения
        }

    override fun attachBaseContext(newBase: Context) {
        // 1) Читаем из SharedPreferences сохранённый код языка
        val prefs = newBase.getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(PrefsKeys.KEY_LANGUAGE, "") ?: ""
        // 2) Переключаем локаль через LocaleHelper (возвращает новый Context с нужной локалью)
        val localizedContext = LocaleHelper.setLocale(newBase, savedCode)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ----------------------
        // 1) Инициализируем AnalyticsManager
        // ----------------------
        AnalyticsManager.init(applicationContext)

        // ----------------------
        // 2) Читаем ранее сохранённый JWT-токен и передаём его в RetrofitClient
        // ----------------------
        val prefs = getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val savedToken = prefs.getString(PrefsKeys.KEY_AUTH_TOKEN, "") ?: ""
        if (savedToken.isNotBlank()) {
            RetrofitClient.setToken(savedToken)
        }

        // ----------------------
        // 3) Запрашиваем разрешение POST_NOTIFICATIONS (Android 13+)
        // ----------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Разрешение уже получено — ничего делать не нужно
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Можно объяснить пользователю, зачем нужно разрешение, а затем запросить ещё раз
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Просто запрашиваем первое разрешение
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        // ----------------------
        // 4) Compose UI
        // ----------------------
        setContent {
            // 4.1) Подписываемся на текущий language code из LanguageViewModel
            val langCode by languageViewModel.languageCodeFlow.collectAsState()

            // Используем `key(langCode) { ... }`, чтобы при смене языка Compose-дерево пересоздалось
            key(langCode) {
                // 4.2) Подписываемся на выбор темы (ThemeOption) из ThemeViewModel
                val themeOption by themeViewModel.themeOption.collectAsState()

                // Определяем, нужна ли тёмная тема
                val isDarkTheme = when (themeOption) {
                    ThemeOption.SYSTEM -> {
                        val currentNightMode =
                            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                        currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    }
                    ThemeOption.LIGHT -> false
                    ThemeOption.DARK -> true
                }

                // Оборачиваем всё в наш CleaningAppTheme
                CleaningAppTheme(darkTheme = isDarkTheme) {
                    // Подключаем навигационный хост (роутинг между экранами)
                    AppNavHost()
                }
            }
        }
    }
}
