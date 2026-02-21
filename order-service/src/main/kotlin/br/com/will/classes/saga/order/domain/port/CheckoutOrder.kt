package br.com.will.classes.saga.order.domain.port

import br.com.will.classes.saga.order.domain.model.Order

interface CheckoutOrder {

    fun execute(orderId: String): Order

}