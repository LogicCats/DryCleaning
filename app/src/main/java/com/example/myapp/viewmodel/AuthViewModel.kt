package com.example.myapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.AuthDTO
import com.example.myapp.data.UserDTO
import com.example.myapp.data.PrefsKeys
import com.example.myapp.network.RetrofitClient
import com.example.myapp.util.AnalyticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // ====== StateFlow для полей ввода (логин/регистрация) ======
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")       // хранит либо введённое имя при регистрации, либо полученное из профиля
    val name: StateFlow<String> = _name.asStateFlow()

    private val _phone = MutableStateFlow("")      // аналогично для телефона
    val phone: StateFlow<String> = _phone.asStateFlow()

    // ====== StateFlow для сообщений об ошибке ======
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // ====== StateFlow для состояния загрузки профиля ======
    private val _isProfileLoading = MutableStateFlow(false)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading.asStateFlow()

    // ====== StateFlow для состояния успешного получения профиля ======
    private val _profileLoaded = MutableStateFlow(false)
    val profileLoaded: StateFlow<Boolean> = _profileLoaded.asStateFlow()

    // ====== StateFlow — пользователь залогинен или нет ======
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()


    // ----- Методы для изменения полей -----
    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun onNameChanged(newName: String) {
        _name.value = newName
    }

    fun onPhoneChanged(newPhone: String) {
        _phone.value = newPhone
    }

    // ====== Метод для установки произвольного сообщения об ошибке ======
    fun setErrorMessage(msg: String) {
        _errorMessage.value = msg
    }


    /**
     * Выполняет сетевой запрос к бэкенду для логина.
     * Если успешный ответ — сохраняем JWT в RetrofitClient и SharedPreferences, сбрасываем ошибку и возвращаем onResult(true).
     * При ошибке — заполняем _errorMessage и возвращаем onResult(false).
     */
    fun login(onResult: (Boolean) -> Unit) {
        val currentEmail    = email.value.trim()
        val currentPassword = password.value.trim()

        // Валидация: ни одно поле не должно быть пустым
        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _errorMessage.value = "Пожалуйста, заполните все поля"
            AnalyticsManager.logEvent("login_failed", "empty_fields")
            onResult(false)
            return
        }

        // Собираем DTO
        val request = AuthDTO.LoginRequest(
            email = currentEmail,
            password = currentPassword
        )

        // Асинхронный запрос
        RetrofitClient.apiService.login(request)
            .enqueue(object : Callback<AuthDTO.AuthResponse> {
                override fun onResponse(
                    call: Call<AuthDTO.AuthResponse>,
                    response: Response<AuthDTO.AuthResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.token.isNotBlank()) {
                            // 1) Сохраняем JWT в RetrofitClient
                            RetrofitClient.setToken(body.token)

                            // 2) Сохраняем JWT также в SharedPreferences
                            viewModelScope.launch {
                                val prefs = getApplication<Application>()
                                    .getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putString(PrefsKeys.KEY_AUTH_TOKEN, body.token)
                                    .apply()
                            }

                            // 3) Помечаем пользователя как «залогиненного»
                            _isLoggedIn.value = true
                            _errorMessage.value = ""
                            AnalyticsManager.logEvent("login_success", "email=$currentEmail")
                            onResult(true)
                        } else {
                            _errorMessage.value = "Не удалось получить токен от сервера"
                            AnalyticsManager.logEvent("login_failed", "no_token,email=$currentEmail")
                            onResult(false)
                        }
                    } else {
                        // Ошибка со стороны сервера (401, 400 и т.д.)
                        val serverMsg = response.errorBody()?.string() ?: "Unknown error"
                        _errorMessage.value = "Ошибка входа: $serverMsg"
                        AnalyticsManager.logEvent("login_failed", "server_error,email=$currentEmail")
                        onResult(false)
                    }
                }

                override fun onFailure(call: Call<AuthDTO.AuthResponse>, t: Throwable) {
                    // Сетевая ошибка
                    _errorMessage.value = "Сетевая ошибка: ${t.localizedMessage}"
                    AnalyticsManager.logEvent("login_failed", "network_error,email=$currentEmail")
                    onResult(false)
                }
            })
    }


    /**
     * Выполняет сетевой запрос к бэкенду для регистрации.
     * Если успешный ответ — сохраняем JWT в RetrofitClient и SharedPreferences, сбрасываем ошибку и возвращаем onResult(true).
     * При ошибке — заполняем _errorMessage и возвращаем onResult(false).
     */
    fun register(onResult: (Boolean) -> Unit) {
        val currentEmail    = email.value.trim()
        val currentPassword = password.value.trim()
        val currentName     = name.value.trim()
        val currentPhone    = phone.value.trim()

        // Валидация: все поля обязательны, пароль не менее 6 символов
        if (currentEmail.isBlank() || currentPassword.isBlank() ||
            currentName.isBlank() || currentPhone.isBlank()
        ) {
            _errorMessage.value = "Пожалуйста, заполните все поля"
            AnalyticsManager.logEvent("register_failed", "empty_fields")
            onResult(false)
            return
        }

        if (currentPassword.length < 6) {
            _errorMessage.value = "Пароль должен содержать минимум 6 символов"
            AnalyticsManager.logEvent("register_failed", "password_too_short")
            onResult(false)
            return
        }

        // Собираем DTO для регистрации
        val request = AuthDTO.RegisterRequest(
            email    = currentEmail,
            password = currentPassword,
            name     = currentName,
            phone    = currentPhone
        )

        RetrofitClient.apiService.register(request)
            .enqueue(object : Callback<AuthDTO.AuthResponse> {
                override fun onResponse(
                    call: Call<AuthDTO.AuthResponse>,
                    response: Response<AuthDTO.AuthResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.token.isNotBlank()) {
                            // 1) Сохраняем JWT в RetrofitClient
                            RetrofitClient.setToken(body.token)

                            // 2) Сохраняем JWT также в SharedPreferences
                            viewModelScope.launch {
                                val prefs = getApplication<Application>()
                                    .getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putString(PrefsKeys.KEY_AUTH_TOKEN, body.token)
                                    .apply()
                            }

                            // 3) Помечаем пользователя как «залогиненного»
                            _isLoggedIn.value = true
                            _errorMessage.value = ""
                            AnalyticsManager.logEvent("register_success", "email=$currentEmail")
                            onResult(true)
                        } else {
                            _errorMessage.value = "Не удалось получить токен от сервера"
                            AnalyticsManager.logEvent("register_failed", "no_token,email=$currentEmail")
                            onResult(false)
                        }
                    } else {
                        val serverMsg = response.errorBody()?.string() ?: "Unknown error"
                        _errorMessage.value = "Ошибка регистрации: $serverMsg"
                        AnalyticsManager.logEvent("register_failed", "server_error,email=$currentEmail")
                        onResult(false)
                    }
                }

                override fun onFailure(call: Call<AuthDTO.AuthResponse>, t: Throwable) {
                    _errorMessage.value = "Сетевая ошибка: ${t.localizedMessage}"
                    AnalyticsManager.logEvent("register_failed", "network_error,email=$currentEmail")
                    onResult(false)
                }
            })
    }


    /**
     * Выполняет сетевой запрос к бэкенду для получения профиля текущего пользователя.
     * Сохраняет имя/почту/телефон в соответствующих StateFlow.
     */
    fun fetchProfile(onResult: (Boolean) -> Unit = {}) {
        // Если пользователь не залогинен, просто возвращаем false
        if (!_isLoggedIn.value) {
            onResult(false)
            return
        }

        _isProfileLoading.value = true
        _errorMessage.value = ""

        RetrofitClient.apiService.getProfile()
            .enqueue(object : Callback<UserDTO.UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserDTO.UserProfileResponse>,
                    response: Response<UserDTO.UserProfileResponse>
                ) {
                    _isProfileLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Заполняем StateFlow данными профиля
                            _name.value = body.name
                            _email.value = body.email
                            _phone.value = body.phone
                            _profileLoaded.value = true
                            AnalyticsManager.logEvent("profile_fetched", "userId=${body.id}")
                            onResult(true)
                        } else {
                            _errorMessage.value = "Не удалось получить данные профиля"
                            AnalyticsManager.logEvent("profile_failed", "empty_body")
                            onResult(false)
                        }
                    } else {
                        val serverMsg = response.errorBody()?.string() ?: "Unknown error"
                        _errorMessage.value = "Ошибка при получении профиля: $serverMsg"
                        AnalyticsManager.logEvent("profile_failed", "server_error")
                        onResult(false)
                    }
                }

                override fun onFailure(call: Call<UserDTO.UserProfileResponse>, t: Throwable) {
                    _isProfileLoading.value = false
                    _errorMessage.value = "Сетевая ошибка при получении профиля: ${t.localizedMessage}"
                    AnalyticsManager.logEvent("profile_failed", "network_error")
                    onResult(false)
                }
            })
    }


    /**
     * Выполняет сетевой запрос к бэкенду для обновления профиля текущего пользователя.
     * По успешному ответу обновляет локальные StateFlow name/phone.
     */
    fun updateProfile(
        newName: String,
        newPhone: String,
        onResult: (Boolean) -> Unit
    ) {
        // Если пользователь не залогинен, сразу возвращаем false
        if (!_isLoggedIn.value) {
            onResult(false)
            return
        }

        // Готовим DTO для обновления
        val request = UserDTO.UserUpdateRequest(
            name  = newName,
            phone = newPhone
        )

        RetrofitClient.apiService.updateProfile(request)
            .enqueue(object : Callback<UserDTO.UserProfileResponse> {
                override fun onResponse(
                    call: Call<UserDTO.UserProfileResponse>,
                    response: Response<UserDTO.UserProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            // Обновляем локальные поля
                            _name.value = body.name
                            _phone.value = body.phone
                            _errorMessage.value = ""
                            AnalyticsManager.logEvent("profile_updated", "name=$newName,phone=$newPhone")
                            onResult(true)
                        } else {
                            _errorMessage.value = "Не удалось получить обновлённые данные профиля"
                            AnalyticsManager.logEvent("profile_update_failed", "empty_body")
                            onResult(false)
                        }
                    } else {
                        val serverMsg = response.errorBody()?.string() ?: "Unknown error"
                        _errorMessage.value = "Ошибка обновления профиля: $serverMsg"
                        AnalyticsManager.logEvent("profile_update_failed", "server_error")
                        onResult(false)
                    }
                }

                override fun onFailure(call: Call<UserDTO.UserProfileResponse>, t: Throwable) {
                    _errorMessage.value = "Сетевая ошибка при обновлении профиля: ${t.localizedMessage}"
                    AnalyticsManager.logEvent("profile_update_failed", "network_error")
                    onResult(false)
                }
            })
    }


    /**
     * Логаут: сбрасываем локальные StateFlow и JWT в RetrofitClient, а также удаляем токен из SharedPreferences.
     */
    fun logout() {
        _isLoggedIn.value    = false
        _name.value          = ""
        _email.value         = ""
        _password.value      = ""
        _phone.value         = ""
        _errorMessage.value  = ""
        _profileLoaded.value = false

        // 1) Сброс токена в RetrofitClient
        RetrofitClient.setToken("")

        // 2) Удаляем сохранённый токен из SharedPreferences
        viewModelScope.launch {
            val prefs = getApplication<Application>()
                .getSharedPreferences(PrefsKeys.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(PrefsKeys.KEY_AUTH_TOKEN)
                .apply()
        }

        AnalyticsManager.logEvent("logout", "")
    }
}
