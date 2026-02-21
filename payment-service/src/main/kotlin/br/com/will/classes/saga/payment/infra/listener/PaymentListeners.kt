package br.com.will.classes.saga.payment.infra.listener

import br.com.will.classes.saga.payment.dto.OrderDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Component
class PaymentListeners(private val snsClient: SnsClient) {
    private val mapper = jacksonObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)
    private val topicArn = System.getenv("ORDER_ACTION_TOPIC_ARN") ?: "arn:aws:sns:us-east-1:000000000000:ORDER_ACTION"

    @SqsListener("PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE")
    fun onOrderCheckout(@Payload message: String) {
        val order = mapper.readValue(message, OrderDTO::class.java)
        log.info("[Payment] Received checkout for order=${order.orderId} status=${order.status}")

        val total = order.items.sumOf { it.price * it.quantity }
        if ((total.toInt() % 2) == 0) {
            log.info("[Payment] Payment success for order=${order.orderId}")
            val updated = order.copy(status = "ORDER_PAID")
            publishOrder(updated)
        } else {
            log.info("[Payment] Payment failed for order=${order.orderId}")
            val updated = order.copy(status = "PAYMENT_FAILED")
            publishOrder(updated)
        }
    }

    @SqsListener("PAYMENT_SERVICE_ORDER_REVERT_QUEUE")
    fun onOrderRevert(@Payload message: String) {
        val order = mapper.readValue(message, OrderDTO::class.java)
        log.info("[Payment] Received revert for order=${order.orderId} status=${order.status}")
        val updated = order.copy(status = "ORDER_PAID_REVERSED")
        publishOrder(updated)
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
