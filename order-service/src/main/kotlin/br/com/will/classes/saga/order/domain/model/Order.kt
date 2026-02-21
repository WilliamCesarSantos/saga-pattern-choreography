package br.com.will.classes.saga.order.domain.model

import java.time.Instant

data class Order(
    val id: String,
    val customerId: String,
    val createdAt: Instant,
    val status: String
)