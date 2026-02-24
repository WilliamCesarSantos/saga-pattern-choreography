package br.com.will.classes.saga.payment.domain.model

import java.math.BigDecimal

data class PaymentTransaction(
    val orderId: String,
    val status: String,
    val amount: BigDecimal,
    val transactionId: String
)

