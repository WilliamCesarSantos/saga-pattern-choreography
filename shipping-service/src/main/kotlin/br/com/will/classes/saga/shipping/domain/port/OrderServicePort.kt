package br.com.will.classes.saga.shipping.domain.port

import br.com.will.classes.saga.shared.model.Order

interface OrderServicePort {
    fun findOrderById(orderId: Long): Order?
}
