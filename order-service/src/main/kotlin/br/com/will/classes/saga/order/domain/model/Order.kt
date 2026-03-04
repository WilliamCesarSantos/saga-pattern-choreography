package br.com.will.classes.saga.order.domain.model

import java.math.BigDecimal
import java.time.Instant

class Order(
    val id: Long,
    var status: String,
    var createdAt: Instant? = null,
    var customer: Customer? = null
) {
    var items: MutableList<OrderItem> = mutableListOf()

    fun calculateTotal(): BigDecimal =
        items.fold(BigDecimal.ZERO) { acc, item -> acc + item.price * item.quantity.toBigDecimal() }
}
