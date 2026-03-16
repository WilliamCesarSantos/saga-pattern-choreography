package br.com.will.classes.saga.notification.application.usecase

import br.com.will.classes.saga.notification.application.port.EmailSenderPort
import br.com.will.classes.saga.notification.domain.model.OrderNotification
import org.springframework.stereotype.Component

@Component
class NotifyCustomerUseCase(
    private val emailSenderPort: EmailSenderPort
) {

    fun execute(notification: OrderNotification) {
        val (subject, body) = resolveMessage(notification.orderId, notification.status)
        emailSenderPort.send(
            to = notification.customerEmail,
            subject = subject,
            body = body
        )
    }

    private fun resolveMessage(orderId: Long, status: String): Pair<String, String> =
        when (status) {
            "ORDER_CREATED" -> Pair(
                "Pedido criado",
                "Seu pedido $orderId foi criado com sucesso e está sendo processado."
            )
            "ORDER_DELIVERED" -> Pair(
                "Pedido entregue",
                "Seu pedido $orderId foi entregue com sucesso. Obrigado pela preferência!"
            )
            "ORDER_NOT_DELIVERED" -> Pair(
                "Problema na entrega",
                "Infelizmente não foi possível entregar seu pedido $orderId. Entre em contato conosco."
            )
            "ORDER_CANCELLED" -> Pair(
                "Pedido cancelado",
                "Seu pedido $orderId foi cancelado."
            )
            else -> Pair(
                "Atualização do pedido",
                "Houve uma atualização no seu pedido $orderId. Status: $status"
            )
        }
}

