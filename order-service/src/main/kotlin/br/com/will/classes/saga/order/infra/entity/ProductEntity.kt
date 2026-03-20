package br.com.will.classes.saga.order.infra.entity

import br.com.will.classes.saga.shared.model.Product
import jakarta.persistence.*

@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val description: String = ""
) {
    fun toDomain(): Product = Product(id = id, description = description)
}
