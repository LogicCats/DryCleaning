package com.example.myapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.myapp.data.OrderRepository

class OrderListViewModel : ViewModel() {
    val orders = mutableStateListOf<com.example.myapp.data.Order>().apply {
        addAll(OrderRepository.getAll())
    }
}
