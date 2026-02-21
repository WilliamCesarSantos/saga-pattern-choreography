package br.com.will.classes.saga.order.infra.listener

import br.com.will.classes.saga.order.domain.port.UpdateStatusOrder
import br.com.will.classes.saga.order.dto.OrderDTO
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OrderStatusListener(
    private val updateStatusOrder: UpdateStatusOrder
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${order-service.sqs.order-status.queue}")
    fun onOrderStatus(orderDTO: OrderDTO) {
        log.info("Received order status update: orderId=${orderDTO.orderId} status=${orderDTO.status}")
        updateStatusOrder.execute(orderDTO.orderId, orderDTO.status)
    }
}
