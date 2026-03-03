package br.com.will.classes.saga.inventory.infra.entity

import br.com.will.classes.saga.inventory.domain.model.Inventory
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "inventory")
data class InventoryEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "product_id", nullable = false, unique = true)
    val productId: UUID,

    @Column(nullable = false)
    var quantity: Int,

    @Version
    var version: Long = 0
) {
    fun toDomain(): Inventory = Inventory(
        id = id,
        productId = productId,
        quantity = quantity,
        version = version
    )

    companion object {
        fun fromDomain(inventory: Inventory): InventoryEntity = InventoryEntity(
            id = inventory.id,
            productId = inventory.productId,
            quantity = inventory.quantity,
            version = inventory.version
        )
    }
}

