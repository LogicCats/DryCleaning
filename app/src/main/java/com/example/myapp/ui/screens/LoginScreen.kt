package com.example.myapp.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapp.R
import com.example.myapp.viewmodel.AuthViewModel


@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClicked: () -> Unit
) {
    // Управление видимостью пароля — локальное состояние в composable
    var passwordVisible by remember { mutableStateOf(false) }

    // Получаем состояния из ViewModel, чтобы UI реагировал на изменения
    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // Центрируем контент вертикально и задаем padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        EmailInput(
            email = email,
            onEmailChange = authViewModel::onEmailChanged,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordInput(
            password = password,
            onPasswordChange = authViewModel::onPasswordChanged,
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = it },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            ErrorMessage(errorMessage)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.login { success -> if (success) onLoginSuccess() } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.btn_login))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onRegisterClicked,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = stringResource(R.string.btn_register))
        }
    }
}

@Composable
fun EmailInput(email: String, onEmailChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.label_email)) },
        leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = stringResource(R.string.cd_email))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(R.string.label_password)) },
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.cd_password))
        },
        trailingIcon = {
            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                Icon(
                    imageVector = image,
                    contentDescription = stringResource(
                        if (passwordVisible) R.string.cd_hide_password else R.string.cd_show_password
                    )
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.padding(top = 8.dp)
    )
}
