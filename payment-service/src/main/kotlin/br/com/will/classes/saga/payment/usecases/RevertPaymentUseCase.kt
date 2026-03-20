package br.com.will.classes.saga.payment.usecases

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction
import br.com.will.classes.saga.payment.domain.port.PaymentEventPublisher
import br.com.will.classes.saga.payment.domain.port.PaymentRepositoryPort
import br.com.will.classes.saga.payment.domain.port.RevertPayment
import br.com.will.classes.saga.shared.model.Order
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RevertPaymentUseCase(
    private val paymentRepositoryPort: PaymentRepositoryPort,
    private val paymentEventPublisher: PaymentEventPublisher
) : RevertPayment {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(order: Order): PaymentTransaction {
        log.info("[Payment] Reverting payment for orderId=${order.orderId}")

        val existing = paymentRepositoryPort.findByOrderId(order.orderId)
        val transactionId = existing?.transactionId ?: UUID.randomUUID().toString()
        val amount = existing?.amount ?: order.calculateTotal()

        val reversal = PaymentTransaction(
            orderId = order.orderId,
            status = "ORDER_PAID_REVERSED",
            amount = amount,
            transactionId = "REVERSAL-$transactionId"
        )
        paymentRepositoryPort.save(reversal)
        log.info("[Payment] Payment reversed — orderId=${order.orderId}")

        val updatedOrder = order.copy(status = "ORDER_PAID_REVERSED")
        paymentEventPublisher.publish(updatedOrder)

        return reversal
    }
}



