package br.com.will.classes.saga.shipping.domain.model

import java.time.LocalDateTime
import java.util.UUID

enum class ShippingStatus {
    PENDING,
    DELIVERED,
    FAILED
}

data class Shipping(
    val id: Long? = null,
    val orderId: String,
    val customerName: String,
    val customerEmail: String,
    val trackingNumber: String = UUID.randomUUID().toString(),
    val status: ShippingStatus = ShippingStatus.PENDING,
    val receivedBy: String? = null,
    val failureReason: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

