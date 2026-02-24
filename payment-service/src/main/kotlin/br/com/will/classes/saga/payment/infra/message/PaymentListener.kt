package br.com.will.classes.saga.payment.infra.message

import br.com.will.classes.saga.payment.domain.port.ProcessPayment
import br.com.will.classes.saga.payment.domain.port.RevertPayment
import br.com.will.classes.saga.shared.dto.OrderDTO
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PaymentListener(
    private val processPayment: ProcessPayment,
    private val revertPayment: RevertPayment,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${payment-service.sqs.order-checkout.queue-name}")
    fun onOrderCheckout(message: String) {
        log.info("[Payment] Received message from checkout queue")
        val orderDTO = objectMapper.readValue(message, OrderDTO::class.java)
        log.info("[Payment] Processing checkout for orderId=${orderDTO.orderId}")
        processPayment.execute(orderDTO)
    }

    @SqsListener($$"${payment-service.sqs.order-reverse.queue-name}")
    fun onOrderRevert(message: String) {
        log.info("[Payment] Received message from revert queue")
        val orderDTO = objectMapper.readValue(message, OrderDTO::class.java)
        log.info("[Payment] Processing revert for orderId=${orderDTO.orderId}")
        revertPayment.execute(orderDTO)
    }
}

