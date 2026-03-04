package br.com.will.classes.saga.inventory.infra.entity

import br.com.will.classes.saga.inventory.domain.model.Inventory
import jakarta.persistence.*

@Entity
@Table(name = "inventory")
data class InventoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_id", nullable = false, unique = true)
    val productId: Long,

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
