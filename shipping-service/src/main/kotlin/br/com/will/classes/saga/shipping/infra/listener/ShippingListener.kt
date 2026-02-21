package br.com.will.classes.saga.shipping.infra.listener

import br.com.will.classes.saga.shipping.dto.OrderDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Component
class ShippingListener(private val snsClient: SnsClient) {
    private val mapper = jacksonObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)
    private val topicArn = System.getenv("ORDER_ACTION_TOPIC_ARN") ?: "arn:aws:sns:us-east-1:000000000000:ORDER_ACTION"

    @SqsListener("SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE")
    fun onInventoryWriteOff(message: String) {
        val order = mapper.readValue(message, OrderDTO::class.java)
        log.info("[Shipping] Received inventory write off for order=${order.orderId}")
        // create shipment record in DB (omitted) and simulate immediate success
        publishOrder(order.copy(status = "ORDER_DELIVERED"))
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
