package br.com.will.classes.saga.payment.infra.repository

import br.com.will.classes.saga.payment.infra.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findByOrderId(orderId: Long): PaymentEntity?
}

