package br.com.will.classes.saga.order.infra.message

import br.com.will.classes.saga.order.domain.port.UpdateStatusOrder
import br.com.will.classes.saga.shared.model.Order
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OrderStatusListener(
    private val updateStatusOrder: UpdateStatusOrder
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${order-service.sqs.order-status.queue-name}")
    fun onOrderStatus(order: Order) {
        log.info("Received order status update: orderId=${order.orderId} status=${order.status}")
        updateStatusOrder.execute(order.orderId, order.status)
    }
}