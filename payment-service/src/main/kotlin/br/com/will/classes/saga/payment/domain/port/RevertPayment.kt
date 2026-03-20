package br.com.will.classes.saga.payment.domain.port

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction
import br.com.will.classes.saga.shared.model.Order

interface RevertPayment {
    fun execute(order: Order): PaymentTransaction
}

