package br.com.will.classes.saga.order.domain.port

import br.com.will.classes.saga.order.domain.model.Order

interface UpdateStatusOrder {

    fun execute(orderId: Long, newState: String): Order
}