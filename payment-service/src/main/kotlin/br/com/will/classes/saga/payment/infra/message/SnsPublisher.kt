package br.com.will.classes.saga.payment.infra.message

import br.com.will.classes.saga.payment.domain.port.PaymentEventPublisher
import br.com.will.classes.saga.shared.dto.OrderDTO
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.ObjectMapper

@Component
class SnsPublisher(
    private val snsTemplate: SnsTemplate,
    private val objectMapper: ObjectMapper,
    @param:Value($$"${payment-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : PaymentEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(orderDTO: OrderDTO) {
        val message = objectMapper.writeValueAsString(orderDTO)
        snsTemplate.convertAndSend(topicArn, message)
        log.info("[Payment] Published event to ORDER_ACTION — orderId=${orderDTO.orderId} status=${orderDTO.status}")
    }
}

