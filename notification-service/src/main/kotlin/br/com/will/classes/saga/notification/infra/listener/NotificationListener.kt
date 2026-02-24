package br.com.will.classes.saga.notification.infra.listener

import br.com.will.classes.saga.shared.dto.OrderDTO
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NotificationListener {
    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("NOTIFICATION_SERVICE_ORDER_QUEUE")
    fun onOrderEvent(message: String) {
        val order = mapper.readValue(message, OrderDTO::class.java)
        val customer = order.customer
        val status = order.status
        val msg = when (status) {
            "ORDER_CHECKOUT" -> "Your order ${order.orderId} was checked out."
            "ORDER_PAID" -> "Your order ${order.orderId} has been paid."
            "OUT_OF_STOCK" -> "Your order ${order.orderId} is out of stock."
            "ORDER_DELIVERED" -> "Your order ${order.orderId} was delivered."
            "ORDER_NOT_DELIVERED" -> "There was a delivery problem with order ${order.orderId}."
            else -> "Update on your order ${order.orderId}: status=$status"
        }
        log.info("[Notification] To=${customer.email} - $msg")
    }
}
