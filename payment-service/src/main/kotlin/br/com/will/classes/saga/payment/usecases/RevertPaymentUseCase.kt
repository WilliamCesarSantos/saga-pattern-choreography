package br.com.will.classes.saga.payment.usecases

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction
import br.com.will.classes.saga.payment.domain.port.PaymentEventPublisher
import br.com.will.classes.saga.payment.domain.port.RevertPayment
import br.com.will.classes.saga.payment.infra.entity.PaymentEntity
import br.com.will.classes.saga.payment.infra.repository.PaymentRepository
import br.com.will.classes.saga.shared.dto.OrderDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RevertPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val paymentEventPublisher: PaymentEventPublisher
) : RevertPayment {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(orderDTO: OrderDTO): PaymentTransaction {
        log.info("[Payment] Reverting payment for orderId=${orderDTO.orderId}")

        // Mock revert — update existing record or insert a reversal entry
        val existing = paymentRepository.findByOrderId(orderDTO.orderId)
        val transactionId = existing?.transactionId ?: UUID.randomUUID().toString()
        val amount = existing?.amount ?: orderDTO.total

        val reversal = PaymentEntity(
            orderId = orderDTO.orderId,
            status = "ORDER_PAID_REVERSED",
            amount = amount,
            transactionId = "REVERSAL-$transactionId"
        )
        paymentRepository.save(reversal)
        log.info("[Payment] Payment reversed — orderId=${orderDTO.orderId}")

        val updatedOrder = orderDTO.copy(status = "ORDER_PAID_REVERSED")
        paymentEventPublisher.publish(updatedOrder)

        return PaymentTransaction(
            orderId = orderDTO.orderId,
            status = "ORDER_PAID_REVERSED",
            amount = amount,
            transactionId = "REVERSAL-$transactionId"
        )
    }
}

