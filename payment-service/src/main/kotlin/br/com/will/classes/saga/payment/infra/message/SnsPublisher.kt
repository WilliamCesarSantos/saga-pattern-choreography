package br.com.will.classes.saga.payment.infra.message

import br.com.will.classes.saga.payment.domain.port.PaymentEventPublisher
import br.com.will.classes.saga.shared.model.Order
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SnsPublisher(
    private val snsTemplate: SnsTemplate,
    @param:Value($$"${payment-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : PaymentEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(order: Order) {
        snsTemplate.convertAndSend(topicArn, order)
        log.info("[Payment] Published event to ORDER_ACTION — orderId=${order.orderId} status=${order.status}")
    }
}

