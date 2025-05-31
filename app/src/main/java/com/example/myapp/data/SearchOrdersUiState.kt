package com.example.myapp.data

data class SearchOrdersUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<String> = emptyList(),
    val error: String? = null,
    val history: List<String> = emptyList()
)