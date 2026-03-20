package br.com.will.classes.saga.order.domain.port

import br.com.will.classes.saga.shared.model.Order

interface CheckoutOrder {

    fun execute(orderId: Long): Order

}