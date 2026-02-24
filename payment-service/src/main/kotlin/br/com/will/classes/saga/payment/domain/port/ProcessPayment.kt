package br.com.will.classes.saga.payment.domain.port

import br.com.will.classes.saga.payment.domain.model.PaymentTransaction
import br.com.will.classes.saga.shared.dto.OrderDTO

interface ProcessPayment {
    fun execute(orderDTO: OrderDTO): PaymentTransaction
}

