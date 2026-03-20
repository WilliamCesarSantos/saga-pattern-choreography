package br.com.will.classes.saga.inventory.infra.message

import br.com.will.classes.saga.inventory.domain.port.InventoryEventPublisher
import br.com.will.classes.saga.shared.model.Order
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SnsPublisher(
    private val snsTemplate: SnsTemplate,
    @param:Value($$"${inventory-service.sns.order-action.topic-arn}")
    private val topicArn: String
) : InventoryEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(order: Order) {
        snsTemplate.convertAndSend(topicArn, order)
        log.info("[Inventory] Published event to ORDER_ACTION — orderId=${order.orderId} status=${order.status}")
    }
}

