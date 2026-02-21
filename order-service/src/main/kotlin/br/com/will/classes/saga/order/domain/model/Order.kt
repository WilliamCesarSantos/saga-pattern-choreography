package br.com.will.classes.saga.order.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders")
class Order(
    @Id
    val id: String,
    var status: String,
    var createdAt: Instant? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var customer: Customer? = null
) {
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    var items: MutableList<OrderItem> = mutableListOf()

    fun calculateTotal(): BigDecimal =
        items.fold(BigDecimal.ZERO) { acc, item -> acc + item.price * item.quantity.toBigDecimal() }
}
