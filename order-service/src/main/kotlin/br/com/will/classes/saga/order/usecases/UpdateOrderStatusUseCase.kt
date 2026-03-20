package br.com.will.classes.saga.order.usecases

import br.com.will.classes.saga.order.domain.exception.OrderNotFound
import br.com.will.classes.saga.order.domain.port.OrderEventPublisher
import br.com.will.classes.saga.order.domain.port.UpdateStatusOrder
import br.com.will.classes.saga.order.domain.repository.OrderRepository
import br.com.will.classes.saga.shared.model.Order
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UpdateOrderStatusUseCase(
    private val orderRepository: OrderRepository
) : UpdateStatusOrder {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(orderId: Long, newState: String): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFound("Order ${orderId} not found") }

        val previousStatus = order.status
        val resolvedStatus = when (newState) {
            "ORDER_PAID_REVERSED" -> "CANCELLED"
            "ORDER_DELIVERED" -> "FINISHED"
            else -> newState
        }

        if (previousStatus == resolvedStatus) {
            log.info("Order ${order.orderId} already at status=$resolvedStatus — skipping update and publish")
            return order
        }

        val copy = order.copy(status = resolvedStatus)
        orderRepository.save(copy)
        log.info("Order ${copy.orderId} updated from $previousStatus to $resolvedStatus")

        return copy
    }
}
