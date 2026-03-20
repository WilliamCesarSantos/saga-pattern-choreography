package br.com.will.classes.saga.order.infra.mapper

import br.com.will.classes.saga.shared.model.Customer
import br.com.will.classes.saga.shared.model.Order
import br.com.will.classes.saga.shared.model.OrderItem
import br.com.will.classes.saga.shared.model.Product
import java.time.Instant

object OrderMapper {

    fun toDto(order: Order): Order {
        val items = order.items.map { item ->
            OrderItem(
                id = item.id,
                quantity = item.quantity,
                price = item.price,
                product = Product(
                    id = item.product.id,
                    description = item.product.description
                )
            )
        }
        val customer = order.customer.let {
            Customer(id = it.id, name = it.name, email = it.email)
        }

        return Order(
            orderId = order.orderId,
            createdAt = order.createdAt ?: Instant.now(),
            items = items,
            customer = customer,
            status = order.status
        )
    }
}

fun Order.toDto(): Order = OrderMapper.toDto(this)
