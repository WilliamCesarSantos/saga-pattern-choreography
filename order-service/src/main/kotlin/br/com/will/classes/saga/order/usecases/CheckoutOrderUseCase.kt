package br.com.will.classes.saga.order.usecases

import br.com.will.classes.saga.order.domain.exception.OrderNotFound
import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.domain.port.CheckoutOrder
import br.com.will.classes.saga.order.domain.port.OrderEventPublisher
import br.com.will.classes.saga.order.domain.repository.OrderRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CheckoutOrderUseCase(
    private val orderRepository: OrderRepository,
    private val orderEventPublisher: OrderEventPublisher
) : CheckoutOrder {

    @Transactional
    override fun execute(orderId: String): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFound("Order $orderId not found") }

        order.status = "ORDER_CHECKOUT"
        orderRepository.save(order)

        orderEventPublisher.publish(order)
        return order
    }

}
