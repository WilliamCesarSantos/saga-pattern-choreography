package br.com.will.classes.saga.order.infra.message.mapper

import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.infra.message.event.OrderFinalizedEvent
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OrderEventMapper {

    fun toOrderFinalizedEvent(order: Order): OrderFinalizedEvent {
        return OrderFinalizedEvent(
            orderId = order.id,
            customerId = order.customerId,
            status = order.status,
            finalizedAt = Instant.now()
        )
    }

}