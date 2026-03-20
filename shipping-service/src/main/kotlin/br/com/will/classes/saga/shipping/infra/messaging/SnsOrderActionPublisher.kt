package br.com.will.classes.saga.shipping.infra.messaging

import br.com.will.classes.saga.shared.model.Order
import br.com.will.classes.saga.shipping.domain.port.OrderActionPublisher
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SnsOrderActionPublisher(
    private val snsTemplate: SnsTemplate,
    @param:Value($$"${shipping-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : OrderActionPublisher {


    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(order: Order) {
        log.info("Publishing ORDER_ACTION for orderId={} status={}", order.orderId, order.status)
        snsTemplate.convertAndSend(topicArn, order)
    }
}

