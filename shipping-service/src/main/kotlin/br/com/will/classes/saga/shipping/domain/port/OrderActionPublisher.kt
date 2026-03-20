package br.com.will.classes.saga.shipping.domain.port

import br.com.will.classes.saga.shared.model.Order

interface OrderActionPublisher {
    fun publish(order: Order)
}

