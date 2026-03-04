package br.com.will.classes.saga.order.domain.model

import java.math.BigDecimal

class OrderItem(
    val id: Long = 0,
    val product: Product,
    val quantity: Int,
    val price: BigDecimal
)
