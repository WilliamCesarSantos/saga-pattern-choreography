package br.com.will.classes.saga.order.domain.port

import br.com.will.classes.saga.order.domain.model.Order

interface OrderEventPublisher {
    fun publish(order: Order)
}

