package com.example.myapp

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.myapp.data.ThemeOption
import com.example.myapp.navigation.AppNavHost
import com.example.myapp.ui.theme.CleaningAppTheme
import com.example.myapp.viewmodel.LanguageViewModel
import com.example.myapp.viewmodel.ThemeViewModel
import java.util.Locale
import android.util.Log
import androidx.compose.runtime.key
import com.example.myapp.util.LocaleHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.myapp.analytics.AnalyticsManager

class MainActivity : ComponentActivity() {
    private val languageViewModel: LanguageViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    // Launcher для запроса разрешения POST_NOTIFICATIONS (Android 13+)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Можно отслеживать, если нужно
        }

    override fun attachBaseContext(newBase: android.content.Context) {
        // Считываем сохранённый язык и оборачиваем контекст
        val prefs = newBase.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val savedCode = prefs.getString("selected_language", "") ?: ""
        val localizedContext = LocaleHelper.setLocale(newBase, savedCode)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем AnalyticsManager
        AnalyticsManager.init(applicationContext)

        // Запрашиваем разрешение POST_NOTIFICATIONS, если SDK >= 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Уже есть
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContent {
            val langCode by languageViewModel.languageCodeFlow.collectAsState()

            key(langCode) {
                val themeOption by themeViewModel.themeOption.collectAsState()
                val isDarkTheme = when (themeOption) {
                    ThemeOption.SYSTEM -> {
                        val currentNightMode =
                            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                        currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    }
                    ThemeOption.LIGHT -> false
                    ThemeOption.DARK -> true
                }

                CleaningAppTheme(darkTheme = isDarkTheme) {
                    AppNavHost()
                }
            }
        }
    }
}
