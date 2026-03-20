package br.com.will.classes.saga.inventory.domain.port

import br.com.will.classes.saga.shared.model.Order

interface InventoryEventPublisher {
    fun publish(order: Order)
}

