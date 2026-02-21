package br.com.will.classes.saga.order.infra.message.event

import java.time.Instant

data class OrderFinalizedEvent(
    val orderId: String,
    val customerId: String,
    val status: String,
    val finalizedAt: Instant = Instant.now(),
    val items: List<OrderItemEvent> = emptyList()
)

data class OrderItemEvent(
    val productId: String,
    val quantity: Int,
    val price: Double
)