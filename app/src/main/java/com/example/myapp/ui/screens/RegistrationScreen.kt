package com.example.myapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.myapp.R
import com.example.myapp.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState


@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,      // Экземпляр ViewModel передаём сверху
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    Log.d("ComposeLog", "RegistrationScreen recomposed")
    // 1. Собираем StateFlow из ViewModel, указывая начальное значение ""
    val nameState        by authViewModel.name.collectAsState(initial = "")
    val phoneState       by authViewModel.phone.collectAsState(initial = "")
    val emailState       by authViewModel.email.collectAsState(initial = "")
    val passwordState    by authViewModel.password.collectAsState(initial = "")
    val errorState       by authViewModel.errorMessage.collectAsState(initial = "")

    // 2. Локальный UI‐стейт для видимости пароля и поля "подтвердите пароль"
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var confirmPassword        by remember { mutableStateOf("") }

    // 3. Загружаем строковые ресурсы из strings.xml
    val titleText         = stringResource(R.string.registration_title)
    val labelName         = stringResource(R.string.label_name)
    val labelPhone        = stringResource(R.string.label_phone)
    val labelEmail        = stringResource(R.string.label_email)
    val labelPassword     = stringResource(R.string.label_password)
    val labelConfirmPass  = stringResource(R.string.label_confirm_password)
    val errorMismatch     = stringResource(R.string.error_password_mismatch)
    val btnRegisterText   = stringResource(R.string.btn_register)
    val btnBackText       = stringResource(R.string.btn_back)
    val cdName            = stringResource(R.string.cd_name)
    val cdPhone           = stringResource(R.string.cd_phone)
    val cdEmail           = stringResource(R.string.cd_email)
    val cdPassword        = stringResource(R.string.cd_password)
    val cdConfirmPass     = stringResource(R.string.cd_confirm_password)
    val cdShowPassword    = stringResource(R.string.cd_show_password)
    val cdHidePassword    = stringResource(R.string.cd_hide_password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Заголовок экрана
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле "Имя"
        OutlinedTextField(
            value = nameState,
            onValueChange = { authViewModel.onNameChanged(it) },
            label = { Text(labelName) },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = cdName)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Поле "Телефон"
        OutlinedTextField(
            value = phoneState,
            onValueChange = { authViewModel.onPhoneChanged(it) },
            label = { Text(labelPhone) },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = cdPhone)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Поле "Email"
        OutlinedTextField(
            value = emailState,
            onValueChange = { authViewModel.onEmailChanged(it) },
            label = { Text(labelEmail) },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = cdEmail)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Поле "Пароль"
        OutlinedTextField(
            value = passwordState,
            onValueChange = { authViewModel.onPasswordChanged(it) },
            label = { Text(labelPassword) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = cdPassword)
            },
            trailingIcon = {
                val desc = if (passwordVisible) cdHidePassword else cdShowPassword
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = desc
                    )
                }
            },
            visualTransformation =
            if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Поле "Подтвердите пароль"
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(labelConfirmPass) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = cdConfirmPass)
            },
            trailingIcon = {
                val desc2 = if (confirmPasswordVisible) cdHidePassword else cdShowPassword
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = desc2
                    )
                }
            },
            visualTransformation =
            if (confirmPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Выводим сообщение об ошибке, если оно не пустое
        if (errorState.isNotBlank()) {
            Text(
                text = errorState,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка "Зарегистрироваться"
        Button(
            onClick = {
                // Проверяем: совпадают ли пароли?
                if (passwordState != confirmPassword) {
                    authViewModel.setErrorMessage(errorMismatch) // Исправлено: вызываем метод, а не пытаемся переприсвоить val
                    return@Button
                }
                // Если пароли совпадают, запускаем сетевой вызов регистрации
                authViewModel.register { success ->
                    if (success) {
                        onRegisterSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = btnRegisterText)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка "Назад"
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = btnBackText)
        }
    }
}