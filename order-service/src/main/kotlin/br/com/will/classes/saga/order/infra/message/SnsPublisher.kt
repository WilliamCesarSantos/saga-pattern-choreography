package br.com.will.classes.saga.order.infra.message

import br.com.will.classes.saga.shared.dto.OrderDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.core.SdkBytes

@Component
class SnsPublisher(private val snsClient: SnsClient) {
    private val mapper = jacksonObjectMapper()

    fun publishOrderAction(topicArn: String, order: OrderDTO) {
        val message = mapper.writeValueAsString(order)
        val request = PublishRequest.builder()
            .topicArn(topicArn)
            .message(message)
            .messageAttributes(
                mapOf("status" to software.amazon.awssdk.services.sns.model.MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(order.status)
                    .build())
            .build()

        snsClient.publish(request)
    }
}

