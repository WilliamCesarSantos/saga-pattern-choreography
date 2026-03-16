package br.com.will.classes.saga.order.infra.message

import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.domain.port.OrderEventPublisher
import br.com.will.classes.saga.order.infra.mapper.toDto
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.ObjectMapper

@Component
class SnsPublisher(
    private val snsTemplate: SnsTemplate,
    private val objectMapper: ObjectMapper,
    @Value($$"${order-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : OrderEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(order: Order) {
        val message = objectMapper.writeValueAsString(order.toDto())
        snsTemplate.convertAndSend(topicArn, message)
        log.info("[Order] Published event to ORDER_ACTION — orderId=${order.id} status=${order.status}")
    }
}
