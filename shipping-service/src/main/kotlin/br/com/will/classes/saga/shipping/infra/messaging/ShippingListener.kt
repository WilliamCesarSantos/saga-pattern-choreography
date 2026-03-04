package br.com.will.classes.saga.shipping.infra.messaging

import br.com.will.classes.saga.shared.dto.OrderDTO
import br.com.will.classes.saga.shipping.domain.model.Shipping
import br.com.will.classes.saga.shipping.usecases.ShippingService
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ShippingListener(
    private val shippingService: ShippingService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${shipping-service.sqs.inventory-write-off.queue-name}")
    fun onInventoryWriteOff(orderDTO: OrderDTO) {
        log.info("Received SHIPPING_SERVICE_INVENTORY_WRITE_OFF for orderId={}", orderDTO.orderId)

        val shipping = Shipping(
            orderId = orderDTO.orderId,
            customerName = orderDTO.customer.name,
            customerEmail = orderDTO.customer.email
        )

        val saved = shippingService.createShipping(shipping)
        log.info("Shipping created with trackingNumber={}", saved.trackingNumber)
    }
}