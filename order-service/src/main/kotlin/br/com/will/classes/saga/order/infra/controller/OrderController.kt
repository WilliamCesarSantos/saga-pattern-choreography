package br.com.will.classes.saga.order.infra.controller

import br.com.will.classes.saga.order.domain.repository.OrderRepository
import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.dto.OrderDTO
import br.com.will.classes.saga.order.dto.OrderItemDTO
import br.com.will.classes.saga.order.dto.CustomerDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import java.time.Instant

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderRepository: OrderRepository,
    private val snsClient: SnsClient,
    @Value("\${app.sns.topic-arn:arn:aws:sns:us-east-1:000000000000:ORDER_ACTION}")
    private val topicArn: String
) {
    private val mapper = jacksonObjectMapper()

    @PostMapping("/{orderId}/checkout")
    fun checkout(@PathVariable orderId: String): ResponseEntity<String> {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }
        order.status = "ORDER_CHECKOUT"
        orderRepository.save(order)

        val dto = toDto(order)
        publishOrder(dto)

        return ResponseEntity.ok("Order checkout triggered")
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<Order> {
        val order = orderRepository.findById(orderId).orElseThrow { RuntimeException("Order not found") }
        return ResponseEntity.ok(order)
    }

    private fun toDto(order: Order): OrderDTO {
        val items = order.items.map { OrderItemDTO(it.id, it.quantity, it.price, br.com.will.classes.saga.order.dto.ProductDTO(it.productId, "")) }
        val customer = CustomerDTO(order.id, "unknown", "unknown")
        return OrderDTO(orderId = order.id, createdAt = order.createdAt ?: Instant.now(), items = items, customer = customer, status = order.status)
    }

    private fun publishOrder(order: OrderDTO) {
        val body = mapper.writeValueAsString(order)
        val request = PublishRequest.builder()
            .topicArn(topicArn)
            .message(body)
            .messageAttributes(mapOf("status" to MessageAttributeValue.builder().dataType("String").stringValue(order.status).build()))
            .build()
        snsClient.publish(request)
    }
}