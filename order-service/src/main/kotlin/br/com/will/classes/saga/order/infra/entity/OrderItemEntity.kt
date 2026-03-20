package br.com.will.classes.saga.order.infra.entity

import br.com.will.classes.saga.shared.model.OrderItem
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: ProductEntity? = null,

    val quantity: Int = 0,
    val price: BigDecimal = BigDecimal.ZERO
) {
    fun toDomain(): OrderItem = OrderItem(
        id = id,
        product = product!!.toDomain(),
        quantity = quantity,
        price = price
    )
}
