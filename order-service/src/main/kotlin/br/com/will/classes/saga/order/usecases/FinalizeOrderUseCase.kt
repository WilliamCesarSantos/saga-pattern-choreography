package br.com.will.classes.saga.order.usecases

import br.com.will.classes.saga.order.domain.exception.OrderNotFound
import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.infra.message.OrderEventProducer
import br.com.will.classes.saga.order.domain.repository.OrderRepository
import org.springframework.stereotype.Service

@Service
class FinalizeOrderUseCase(
    private val orderRepository: OrderRepository,
    private val orderEventProducer: OrderEventProducer
) {
    fun execute(orderId: String): Order {
        val order = orderRepository.findById(orderId)
            ?: throw OrderNotFound("Pedido não encontrado com ID: $orderId")

        val finalizedOrder = order.copy(status = "FINALIZED")
        val savedOrder = orderRepository.save(finalizedOrder)
        orderEventProducer.publishOrderFinalized(savedOrder)
        
        return savedOrder
    }
}