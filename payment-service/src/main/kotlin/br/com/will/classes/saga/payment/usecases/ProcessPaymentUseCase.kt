package br.com.will.classes.saga.payment.usecases

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction
import br.com.will.classes.saga.payment.domain.port.PaymentEventPublisher
import br.com.will.classes.saga.payment.domain.port.PaymentRepositoryPort
import br.com.will.classes.saga.payment.domain.port.ProcessPayment
import br.com.will.classes.saga.shared.model.Order
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProcessPaymentUseCase(
    private val paymentRepositoryPort: PaymentRepositoryPort,
    private val paymentEventPublisher: PaymentEventPublisher
) : ProcessPayment {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(order: Order): PaymentTransaction {
        log.info("[Payment] Processing payment for orderId=${order.orderId}")

        val existingPayment = paymentRepositoryPort.findByOrderId(order.orderId)
        if (existingPayment != null) {
            log.info("[Payment] Payment already exists for orderId=${order.orderId}, returning existing transaction")
            return existingPayment
        }

        val transactionId = UUID.randomUUID().toString()
        val amount = order.calculateTotal()

        val transaction = PaymentTransaction(
            orderId = order.orderId,
            status = "ORDER_PAID",
            amount = amount,
            transactionId = transactionId
        )
        paymentRepositoryPort.save(transaction)
        log.info("[Payment] Payment processed — transactionId=$transactionId orderId=${order.orderId}")

        val updatedOrder = order.copy(status = "ORDER_PAID")
        paymentEventPublisher.publish(updatedOrder)

        return transaction
    }
}



