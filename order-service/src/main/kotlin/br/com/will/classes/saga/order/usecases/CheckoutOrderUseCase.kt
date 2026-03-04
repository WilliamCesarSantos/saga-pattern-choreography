package br.com.will.classes.saga.order.usecases

import br.com.will.classes.saga.order.domain.exception.EmptyOrderException
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
    override fun execute(orderId: Long): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFound("Order $orderId not found") }

        if (order.items.isEmpty()) {
            throw EmptyOrderException("Order $orderId must have at least one item to checkout")
        }

        if (order.status != "CREATED") {
            throw IllegalStateException("Order $orderId cannot be checked out from status '${order.status}'. Only CREATED orders are allowed.")
        }

        order.status = "ORDER_CHECKOUT"
        orderRepository.save(order)

        orderEventPublisher.publish(order)
        return order
    }

}
