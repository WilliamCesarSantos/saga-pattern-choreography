package br.com.will.classes.saga.notification.domain.model

data class OrderNotification(
    val orderId: Long,
    val status: String,
    val customerName: String,
    val customerEmail: String
)

