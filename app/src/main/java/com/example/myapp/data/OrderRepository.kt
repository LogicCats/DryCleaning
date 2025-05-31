package com.example.myapp.data


object OrderRepository {
    private val orders = mutableListOf<Order>()

    fun add(order: Order) {
        orders.add(order)
    }

    fun getAll(): List<Order> = orders.toList()

    fun findById(id: String): Order? = orders.find { it.id == id }
}
