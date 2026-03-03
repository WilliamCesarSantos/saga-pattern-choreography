package br.com.will.classes.saga.inventory.domain.port

import br.com.will.classes.saga.shared.dto.OrderDTO

interface InventoryEventPublisher {
    fun publish(orderDTO: OrderDTO)
}

