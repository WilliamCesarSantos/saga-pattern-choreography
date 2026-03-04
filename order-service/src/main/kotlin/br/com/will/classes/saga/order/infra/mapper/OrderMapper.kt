package br.com.will.classes.saga.order.infra.mapper

import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.shared.dto.CustomerDTO
import br.com.will.classes.saga.shared.dto.OrderDTO
import br.com.will.classes.saga.shared.dto.OrderItemDTO
import br.com.will.classes.saga.shared.dto.ProductDTO
import java.time.Instant

object OrderMapper {

    fun toDto(order: Order): OrderDTO {
        val items = order.items.map { item ->
            OrderItemDTO(
                id = item.id,
                quantity = item.quantity,
                price = item.price,
                product = ProductDTO(id = item.product.id, description = item.product.description)
            )
        }
        val customerDto = order.customer?.let {
            CustomerDTO(id = it.id, name = it.name, email = it.email)
        } ?: CustomerDTO(id = 0L, name = "unknown", email = "unknown")

        return OrderDTO(
            orderId = order.id,
            createdAt = order.createdAt ?: Instant.now(),
            items = items,
            customer = customerDto,
            status = order.status,
            total = order.calculateTotal()
        )
    }
}

fun Order.toDto(): OrderDTO = OrderMapper.toDto(this)
