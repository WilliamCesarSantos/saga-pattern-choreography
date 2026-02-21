package br.com.will.classes.saga.order.infra.controller

import br.com.will.classes.saga.order.domain.port.CheckoutOrder
import br.com.will.classes.saga.order.domain.repository.OrderRepository
import br.com.will.classes.saga.order.dto.OrderDTO
import br.com.will.classes.saga.order.infra.mapper.toDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(
    private val checkoutOrder: CheckoutOrder,
    private val orderRepository: OrderRepository
) {
    @PostMapping("/{orderId}/checkout")
    fun checkout(@PathVariable orderId: String): ResponseEntity<OrderDTO> {
        val order = checkoutOrder.execute(orderId)
        return ResponseEntity.ok(order.toDto())
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<OrderDTO> {
        val order = orderRepository.findById(orderId)
            .orElseThrow { RuntimeException("Order not found") }
        return ResponseEntity.ok(order.toDto())
    }
}