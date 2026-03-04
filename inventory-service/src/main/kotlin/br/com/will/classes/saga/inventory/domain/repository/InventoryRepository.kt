package br.com.will.classes.saga.inventory.domain.repository

import br.com.will.classes.saga.inventory.domain.model.Inventory

interface InventoryRepository {
    fun findByProductId(productId: Long): Inventory?
    fun save(inventory: Inventory): Inventory
}
