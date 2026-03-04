package br.com.will.classes.saga.inventory.infra.repository

import br.com.will.classes.saga.inventory.infra.entity.InventoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryJpaRepository : JpaRepository<InventoryEntity, Long> {
    fun findByProductId(productId: Long): InventoryEntity?
}
