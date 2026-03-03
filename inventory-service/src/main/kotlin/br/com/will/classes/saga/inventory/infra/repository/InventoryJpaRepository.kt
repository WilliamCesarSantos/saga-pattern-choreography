package br.com.will.classes.saga.inventory.infra.repository

import br.com.will.classes.saga.inventory.infra.entity.InventoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InventoryJpaRepository : JpaRepository<InventoryEntity, UUID> {
    fun findByProductId(productId: UUID): InventoryEntity?
}

