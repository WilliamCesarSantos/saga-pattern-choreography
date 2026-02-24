package br.com.will.classes.saga.payment.infra.message

import br.com.will.classes.saga.payment.domain.port.PaymentEventPublisher
import br.com.will.classes.saga.shared.dto.OrderDTO
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Component
class SnsPublisher(
    private val snsClient: SnsClient,
    private val objectMapper: ObjectMapper,
    @param:Value("\${app.sns.topic-arn}")
    private val topicArn: String
) : PaymentEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(orderDTO: OrderDTO) {
        val message = objectMapper.writeValueAsString(orderDTO)
        val request = PublishRequest.builder()
            .topicArn(topicArn)
            .message(message)
            .messageAttributes(
                mapOf(
                    "status" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(orderDTO.status)
                        .build()
                )
            )
            .build()
        snsClient.publish(request)
        log.info("[Payment] Published event to ORDER_ACTION — orderId=${orderDTO.orderId} status=${orderDTO.status}")
    }
}

