package br.com.will.classes.saga.notification.infra.listener

import br.com.will.classes.saga.shared.model.Order
import br.com.will.classes.saga.notification.application.usecase.NotifyCustomerUseCase
import br.com.will.classes.saga.notification.domain.model.OrderNotification
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NotificationListener(
    private val notifyCustomerUseCase: NotifyCustomerUseCase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${notification-service.sqs.order.queue-name}")
    fun onMessage(order: Order) {
        logger.info("Mensagem recebida da fila NOTIFICATION_SERVICE_ORDER: orderId=${order.orderId}, status=${order.status}")
        notifyCustomerUseCase.execute(order.toDomain())
    }

    private fun Order.toDomain() = OrderNotification(
        orderId = this.orderId,
        status = this.status,
        customerName = this.customer.name,
        customerEmail = this.customer.email
    )
}

