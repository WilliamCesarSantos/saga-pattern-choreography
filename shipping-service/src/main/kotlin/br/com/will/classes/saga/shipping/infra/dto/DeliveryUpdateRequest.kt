package br.com.will.classes.saga.shipping.infra.dto

data class DeliveryUpdateRequest(
    val success: Boolean,
    val receivedBy: String? = null,
    val failureReason: String? = null
)

