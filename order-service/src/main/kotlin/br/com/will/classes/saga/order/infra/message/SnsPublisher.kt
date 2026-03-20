package br.com.will.classes.saga.order.infra.message

import br.com.will.classes.saga.order.domain.port.OrderEventPublisher
import br.com.will.classes.saga.order.infra.mapper.toDto
import br.com.will.classes.saga.shared.model.Order
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SnsPublisher(
    private val snsTemplate: SnsTemplate,
    @Value($$"${order-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : OrderEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(order: Order) {
        snsTemplate.convertAndSend(topicArn, order.toDto())
        log.info("[Order] Published event to ORDER_ACTION — orderId=${order.orderId} status=${order.status}")
    }
}
