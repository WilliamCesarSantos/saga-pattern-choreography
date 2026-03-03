package br.com.will.classes.saga.payment.usecases

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction
import br.com.will.classes.saga.payment.domain.port.PaymentEventPublisher
import br.com.will.classes.saga.payment.domain.port.ProcessPayment
import br.com.will.classes.saga.payment.infra.entity.PaymentEntity
import br.com.will.classes.saga.payment.infra.repository.PaymentRepository
import br.com.will.classes.saga.shared.dto.OrderDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val paymentEventPublisher: PaymentEventPublisher
) : ProcessPayment {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(orderDTO: OrderDTO): PaymentTransaction {
        log.info("[Payment] Processing payment for orderId=${orderDTO.orderId}")

        // Mock payment processing
        val transactionId = UUID.randomUUID().toString()
        val amount = orderDTO.total

        // TODO use case should use model, never use adapter entity.
        val entity = PaymentEntity(
            orderId = orderDTO.orderId,
            status = "ORDER_PAID",
            amount = amount,
            transactionId = transactionId
        )
        paymentRepository.save(entity)
        log.info("[Payment] Payment processed — transactionId=$transactionId orderId=${orderDTO.orderId}")

        val updatedOrder = orderDTO.copy(status = "ORDER_PAID")
        paymentEventPublisher.publish(updatedOrder)

        return PaymentTransaction(
            orderId = orderDTO.orderId,
            status = "ORDER_PAID",
            amount = amount,
            transactionId = transactionId
        )
    }
}

