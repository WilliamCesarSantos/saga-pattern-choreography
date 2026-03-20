package br.com.will.classes.saga.order.infra.entity

import br.com.will.classes.saga.shared.model.Order
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
            orderId = id,
            status = status,
            createdAt = createdAt ?: Instant.now(),
            items = items.map { it.toDomain() }.toList(),
            customer = customer!!.toDomain()
        )
        return order
    }

    companion object {
        fun fromDomain(order: Order, customerEntity: CustomerEntity?): OrderEntity {
            return OrderEntity(
                id = order.orderId,
                status = order.status,
                createdAt = order.createdAt,
                customer = customerEntity
            )
        }
    }
}
