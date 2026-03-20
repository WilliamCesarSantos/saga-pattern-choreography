package br.com.will.classes.saga.order.infra.repository

import br.com.will.classes.saga.order.domain.repository.OrderRepository
import br.com.will.classes.saga.order.infra.entity.OrderEntity
import br.com.will.classes.saga.shared.model.Order
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepository {

    override fun findById(id: Long): Optional<Order> =
        orderJpaRepository.findById(id).map { it.toDomain() }

    override fun save(order: Order): Order {
        val existing = orderJpaRepository.findById(order.orderId)
        val entity = if (existing.isPresent) {
            val e = existing.get()
            e.status = order.status
            e
        } else {
            OrderEntity.fromDomain(order, null)
        }
        return orderJpaRepository.save(entity).toDomain()
    }
}
