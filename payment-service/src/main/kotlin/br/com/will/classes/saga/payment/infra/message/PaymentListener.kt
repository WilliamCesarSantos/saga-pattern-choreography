package br.com.will.classes.saga.payment.infra.message

import br.com.will.classes.saga.payment.domain.port.ProcessPayment
import br.com.will.classes.saga.payment.domain.port.RevertPayment
import br.com.will.classes.saga.shared.model.Order
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PaymentListener(
    private val processPayment: ProcessPayment,
    private val revertPayment: RevertPayment
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${payment-service.sqs.order-checkout.queue-name}")
    fun onOrderCheckout(order: Order) {
        log.info("[Payment] Processing checkout for orderId=${order.orderId}")
        processPayment.execute(order)
    }

    @SqsListener($$"${payment-service.sqs.payment-revert.queue-name}")
    fun onOrderRevert(order: Order) {
        log.info("[Payment] Processing revert for orderId=${order.orderId}")
        revertPayment.execute(order)
    }
}

