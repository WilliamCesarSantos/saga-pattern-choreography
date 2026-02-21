package br.com.will.classes.saga.payment.dto

import java.time.Instant

data class ProductDTO(
    val id: String,
    val description: String
)

data class OrderItemDTO(
    val id: String,
    val quantity: Int,
    val price: Double,
    val product: ProductDTO
)

data class CustomerDTO(
    val id: String,
    val name: String,
    val email: String
)

data class OrderDTO(
    val orderId: String,
    val createdAt: Instant,
    val items: List<OrderItemDTO>,
    val customer: CustomerDTO,
    val status: String
)

