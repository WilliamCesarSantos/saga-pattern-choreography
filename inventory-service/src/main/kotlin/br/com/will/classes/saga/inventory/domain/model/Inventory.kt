package br.com.will.classes.saga.inventory.domain.model

data class Inventory(
    val id: Long = 0,
    val productId: Long,
    var quantity: Int,
    var version: Long = 0
)
