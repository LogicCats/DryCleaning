package com.example.myapp.viewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapp.analytics.AnalyticsManager
import kotlinx.coroutines.flow.MutableStateFlow

class AuthViewModel : ViewModel() {

    // Состояния экрана
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var name = mutableStateOf("")
    var phone = mutableStateOf("")

    private val _isLoggedIn = MutableStateFlow(false)
    //val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    var errorMessage = mutableStateOf("")

    fun login(onResult: (Boolean) -> Unit) {
        if (email.value.isBlank() || password.value.isBlank()) {
            errorMessage.value = "Пожалуйста, заполните все поля"
            AnalyticsManager.logEvent("login_failed", "empty_fields")
            onResult(false)
            return
        }
        // Имитируем авторизацию
        if (email.value == "user@example.com" && password.value == "123456") {
            _isLoggedIn.value = true
            errorMessage.value = ""
            AnalyticsManager.logEvent("login_success", "email=${email.value}")
            onResult(true)
        } else {
            errorMessage.value = "Неверный email или пароль"
            AnalyticsManager.logEvent("login_failed", "invalid_credentials,email=${email.value}")
            onResult(false)
        }
    }

    fun register(onResult: (Boolean) -> Unit) {
        if (email.value.isBlank() || password.value.isBlank() || name.value.isBlank() || phone.value.isBlank()) {
            errorMessage.value = "Пожалуйста, заполните все поля"
            AnalyticsManager.logEvent("register_failed", "empty_fields")
            onResult(false)
            return
        }
        if (password.value.length < 6) {
            errorMessage.value = "Пароль должен содержать минимум 6 символов"
            AnalyticsManager.logEvent("register_failed", "password_too_short")
            onResult(false)
            return
        }
        // Здесь можно добавить логику регистрации через сервер
        _isLoggedIn.value = true
        errorMessage.value = ""
        AnalyticsManager.logEvent("register_success", "email=${email.value}")
        onResult(true)
    }

    fun logout() {
        _isLoggedIn.value = false
        email.value = ""
        password.value = ""
        name.value = ""
        phone.value = ""
        AnalyticsManager.logEvent("logout", "")
    }
}
