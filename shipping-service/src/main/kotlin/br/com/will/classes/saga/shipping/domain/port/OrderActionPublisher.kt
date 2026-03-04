package br.com.will.classes.saga.shipping.domain.port

import br.com.will.classes.saga.shared.dto.OrderDTO

interface OrderActionPublisher {
    fun publish(orderDTO: OrderDTO)
}

