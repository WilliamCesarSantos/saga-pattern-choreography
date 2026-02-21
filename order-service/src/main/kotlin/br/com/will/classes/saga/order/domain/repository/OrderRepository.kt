package br.com.will.classes.saga.order.domain.repository

import br.com.will.classes.saga.order.domain.model.Order

interface OrderRepository  {
    fun save(order: Order): Order
    fun findById(id: String): Order?
}