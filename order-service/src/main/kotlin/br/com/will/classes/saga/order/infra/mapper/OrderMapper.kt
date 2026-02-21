package br.com.will.classes.saga.order.infra.mapper

import br.com.will.classes.saga.order.domain.model.Order
import br.com.will.classes.saga.order.dto.CustomerDTO
import br.com.will.classes.saga.order.dto.OrderDTO
import br.com.will.classes.saga.order.dto.OrderItemDTO
import br.com.will.classes.saga.order.dto.ProductDTO
import java.time.Instant

object OrderMapper {

    fun toDto(order: Order): OrderDTO {
        val items = order.items.map {
            OrderItemDTO(
                id = it.id,
                quantity = it.quantity,
                price = it.price,
                product = ProductDTO(id = it.product.id, description = it.product.description)
            )
        }
        val customerDto = order.customer?.let {
            CustomerDTO(id = it.id, name = it.name, email = it.email)
        } ?: CustomerDTO(id = "", name = "unknown", email = "unknown")

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
