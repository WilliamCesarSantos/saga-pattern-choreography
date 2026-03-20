package br.com.will.classes.saga.payment.domain.port

import br.com.will.classes.saga.shared.model.Order

interface PaymentEventPublisher {
    fun publish(order: Order)
}

