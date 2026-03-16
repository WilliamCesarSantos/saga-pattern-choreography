package br.com.will.classes.saga.shared.dto

import java.math.BigDecimal
import java.time.Instant

data class ProductDTO(
    val id: Long,
    val description: String
)

data class OrderItemDTO(
    val id: Long,
    val quantity: Int,
    val price: BigDecimal,
    val product: ProductDTO
)

data class CustomerDTO(
    val id: Long,
    val name: String,
    val email: String
)

data class OrderDTO(
    val orderId: Long,
    val createdAt: Instant,
    val items: List<OrderItemDTO>,
    val customer: CustomerDTO,
    val status: String,
    val total: BigDecimal
)
