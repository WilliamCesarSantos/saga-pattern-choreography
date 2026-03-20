package br.com.will.classes.saga.payment.infra.repository

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction
import br.com.will.classes.saga.payment.domain.port.PaymentRepositoryPort
import br.com.will.classes.saga.payment.infra.entity.PaymentEntity
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryAdapter(
    private val paymentRepository: PaymentRepository
) : PaymentRepositoryPort {

    override fun findByOrderId(orderId: Long): PaymentTransaction? =
        paymentRepository.findByOrderId(orderId)?.toModel()

    override fun save(transaction: PaymentTransaction): PaymentTransaction {
        val entity = PaymentEntity(
            orderId = transaction.orderId,
            status = transaction.status,
            amount = transaction.amount,
            transactionId = transaction.transactionId
        )
        return paymentRepository.save(entity).toModel()
    }

    private fun PaymentEntity.toModel(): PaymentTransaction = PaymentTransaction(
        orderId = orderId,
        status = status,
        amount = amount,
        transactionId = transactionId
    )
}

