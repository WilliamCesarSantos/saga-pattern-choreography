package br.com.will.classes.saga.order.domain.repository

import br.com.will.classes.saga.order.domain.model.Order
import java.util.Optional

interface OrderRepository {
    fun findById(id: Long): Optional<Order>
    fun save(order: Order): Order
}
