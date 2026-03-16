package br.com.will.classes.saga.shipping.infra.messaging

import br.com.will.classes.saga.shared.dto.OrderDTO
import br.com.will.classes.saga.shipping.domain.port.OrderActionPublisher
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class SnsOrderActionPublisher(
    private val snsTemplate: SnsTemplate,
    private val objectMapper: ObjectMapper,
    @param:Value($$"${shipping-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : OrderActionPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(orderDTO: OrderDTO) {
        val message = objectMapper.writeValueAsString(orderDTO)
        log.info("Publishing ORDER_ACTION for orderId={} status={}", orderDTO.orderId, orderDTO.status)
        snsTemplate.sendNotification(topicArn, message, "ORDER_ACTION")
    }
}

