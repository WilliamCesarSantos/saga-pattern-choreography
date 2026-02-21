package br.com.will.classes.saga.order.usecases

import br.com.will.classes.saga.order.domain.exception.OrderNotFound
import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.domain.port.OrderEventPublisher
import br.com.will.classes.saga.order.domain.port.UpdateStatusOrder
import br.com.will.classes.saga.order.domain.repository.OrderRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UpdateOrderStatusUseCase(
    private val orderRepository: OrderRepository,
    private val orderEventPublisher: OrderEventPublisher
) : UpdateStatusOrder {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(orderId: String, newState: String): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFound("Order ${orderId} not found") }

        val previousStatus = order.status
        val resolvedStatus = when (newState) {
            "ORDER_PAID_REVERSED" -> "CANCELLED"
            "ORDER_DELIVERED" -> "FINISHED"
            else -> newState
        }

        if (previousStatus == resolvedStatus) {
            log.info("Order ${order.id} already at status=$resolvedStatus — skipping update and publish")
            return order
        }

        order.status = resolvedStatus
        orderRepository.save(order)
        log.info("Order ${order.id} updated from $previousStatus to $resolvedStatus")

        orderEventPublisher.publish(order)
        log.info("Published ORDER_ACTION for order=${order.id} status=$resolvedStatus")
        return order
    }
}
