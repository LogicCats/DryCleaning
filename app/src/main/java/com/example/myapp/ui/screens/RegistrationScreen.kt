package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.myapp.R
import com.example.myapp.viewmodel.AuthViewModel

@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // Local UI state
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }

    // ViewModel state
    val name by authViewModel.name
    val phone by authViewModel.phone
    val email by authViewModel.email
    val password by authViewModel.password
    val errorMessage by authViewModel.errorMessage

    // Preload string resources
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
        Text(text = titleText, style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { authViewModel.name.value = it },
            label = { Text(labelName) },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = cdName)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { authViewModel.phone.value = it },
            label = { Text(labelPhone) },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = cdPhone)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { authViewModel.email.value = it },
            label = { Text(labelEmail) },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = cdEmail)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { authViewModel.password.value = it },
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
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(labelConfirmPass) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = cdConfirmPass)
            },
            trailingIcon = {
                val desc = if (confirmPasswordVisible) cdHidePassword else cdShowPassword
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = desc
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    authViewModel.errorMessage.value = errorMismatch
                    return@Button
                }
                authViewModel.register { success ->
                    if (success) onRegisterSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(btnRegisterText)
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(btnBackText)
        }
    }
}
