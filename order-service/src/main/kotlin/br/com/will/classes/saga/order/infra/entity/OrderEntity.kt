package br.com.will.classes.saga.order.infra.entity

import br.com.will.classes.saga.order.domain.model.Order
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var status: String = "",

    val createdAt: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    val customer: CustomerEntity? = null
) {
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    var items: MutableList<OrderItemEntity> = mutableListOf()

    fun toDomain(): Order {
        val order = Order(
            id = id,
            status = status,
            createdAt = createdAt,
            customer = customer?.toDomain()
        )
        order.items = items.map { it.toDomain() }.toMutableList()
        return order
    }

    companion object {
        fun fromDomain(order: Order, customerEntity: CustomerEntity?): OrderEntity {
            return OrderEntity(
                id = order.id,
                status = order.status,
                createdAt = order.createdAt,
                customer = customerEntity
            )
        }
    }
}
