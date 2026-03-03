package br.com.will.classes.saga.inventory.domain.model

import java.util.UUID

data class Inventory(
    val id: UUID = UUID.randomUUID(),
    val productId: UUID,
    var quantity: Int,
    var version: Long = 0
)

