package br.com.will.classes.saga.inventory.domain.repository

import br.com.will.classes.saga.inventory.domain.model.Inventory
import java.util.UUID

interface InventoryRepository {
    fun findByProductId(productId: UUID): Inventory?
    fun save(inventory: Inventory): Inventory
}

