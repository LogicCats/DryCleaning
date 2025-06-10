package com.example.myapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapp.R
import com.example.myapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val nameState by authViewModel.name.collectAsState(initial = "")
    val emailState by authViewModel.email.collectAsState(initial = "")
    val phoneState by authViewModel.phone.collectAsState(initial = "")
    val errorState by authViewModel.errorMessage.collectAsState(initial = "")
    val isLoading by authViewModel.isProfileLoading.collectAsState(initial = false)

    // Состояния для диалога редактирования
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var updateError by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        authViewModel.fetchProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            when {
                isLoading -> {
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.loading_profile),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                errorState.isNotBlank() -> {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = errorState,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { authViewModel.fetchProfile() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = stringResource(R.string.btn_retry))
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.profile_name, nameState))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.profile_email, emailState))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.profile_phone, phoneState))
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = {
                        // При открытии диалога подставим текущие значения
                        editedName = nameState
                        editedPhone = phoneState
                        showEditDialog = true
                    }) {
                        Text(text = stringResource(R.string.btn_edit_profile))
                    }
                }
            }
        }
    }

    // Диалог редактирования
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = stringResource(R.string.edit_profile)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text(text = stringResource(R.string.label_name)) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text(text = stringResource(R.string.label_phone)) }
                    )
                    if (updateError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = updateError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            ,
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.updateProfile(editedName, editedPhone) { success ->
                        if (success) {
                            showEditDialog = false
                            updateError = ""
                        } else {
                            updateError = "Не удалось обновить профиль. Попробуйте позже."
                        }
                    }
                }) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}
