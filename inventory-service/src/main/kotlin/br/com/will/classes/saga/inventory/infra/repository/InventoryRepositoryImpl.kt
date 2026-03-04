package br.com.will.classes.saga.inventory.infra.repository

import br.com.will.classes.saga.inventory.domain.model.Inventory
import br.com.will.classes.saga.inventory.domain.repository.InventoryRepository
import br.com.will.classes.saga.inventory.infra.entity.InventoryEntity
import org.springframework.stereotype.Repository

@Repository
class InventoryRepositoryImpl(
    private val jpaRepository: InventoryJpaRepository
) : InventoryRepository {

    override fun findByProductId(productId: Long): Inventory? =
        jpaRepository.findByProductId(productId)?.toDomain()

    override fun save(inventory: Inventory): Inventory =
        jpaRepository.save(InventoryEntity.fromDomain(inventory)).toDomain()
}
