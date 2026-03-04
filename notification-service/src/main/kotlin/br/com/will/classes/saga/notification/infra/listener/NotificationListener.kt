package br.com.will.classes.saga.notification.infra.listener

import br.com.will.classes.saga.shared.dto.OrderDTO
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
    fun onMessage(orderDTO: OrderDTO) {
        logger.info("Mensagem recebida da fila NOTIFICATION_SERVICE_ORDER: orderId=${orderDTO.orderId}, status=${orderDTO.status}")
        notifyCustomerUseCase.execute(orderDTO.toDomain())
    }

    private fun OrderDTO.toDomain() = OrderNotification(
        orderId = this.orderId,
        status = this.status,
        customerName = this.customer.name,
        customerEmail = this.customer.email
    )
}

