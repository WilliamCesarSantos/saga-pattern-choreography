package br.com.will.classes.saga.payment.domain.port

import br.com.will.classes.saga.shared.dto.OrderDTO

interface PaymentEventPublisher {
    fun publish(orderDTO: OrderDTO)
}

