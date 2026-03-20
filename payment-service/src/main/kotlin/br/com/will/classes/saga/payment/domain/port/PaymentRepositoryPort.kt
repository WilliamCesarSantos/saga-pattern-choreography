package br.com.will.classes.saga.payment.domain.port

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction

interface PaymentRepositoryPort {
    fun findByOrderId(orderId: Long): PaymentTransaction?
    fun save(transaction: PaymentTransaction): PaymentTransaction
}

