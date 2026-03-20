package br.com.will.classes.saga.shared.model

import java.math.BigDecimal
import java.time.Instant

data class Product(
    val id: Long,
    val description: String
)

data class OrderItem(
    val id: Long,
    val quantity: Int,
    val price: BigDecimal,
    val product: Product
)

data class Customer(
    val id: Long,
    val name: String,
    val email: String
)

data class Order(
    val orderId: Long,
    val createdAt: Instant,
    val items: List<OrderItem>,
    val customer: Customer,
    val status: String
) {
    fun calculateTotal(): BigDecimal =
        items.fold(BigDecimal.ZERO) { acc, item -> acc + item.price * item.quantity.toBigDecimal() }
}
