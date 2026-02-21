package br.com.will.classes.saga.order.infra.message

import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.domain.port.OrderEventPublisher
import br.com.will.classes.saga.order.infra.mapper.toDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Component
class SnsPublisher(
    private val snsClient: SnsClient,
    private val objectMapper: ObjectMapper,
    @Value($$"${order-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : OrderEventPublisher {

    override fun publish(order: Order) {
        val message = objectMapper.writeValueAsString(order.toDto())
        val request = PublishRequest.builder()
            .topicArn(topicArn)
            .message(message)
            .messageAttributes(
                mapOf(
                    "status" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(order.status)
                        .build()
                )
            )
            .build()
        snsClient.publish(request)
    }
}
