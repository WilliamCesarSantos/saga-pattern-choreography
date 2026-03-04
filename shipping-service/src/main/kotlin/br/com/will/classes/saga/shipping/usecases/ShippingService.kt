package br.com.will.classes.saga.shipping.usecases

import br.com.will.classes.saga.shipping.domain.port.OrderActionPublisher
import br.com.will.classes.saga.shipping.domain.port.OrderServicePort
import br.com.will.classes.saga.shipping.domain.model.Shipping
import br.com.will.classes.saga.shipping.infra.repository.ShippingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ShippingService(
    private val shippingRepository: ShippingRepository,
    private val orderServicePort: OrderServicePort,
    private val orderActionPublisher: OrderActionPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun createShipping(shipping: Shipping): Shipping {
        log.info("Creating shipping for orderId={}", shipping.orderId)
        return shippingRepository.save(shipping)
    }

    fun confirmDelivery(trackingNumber: String, receivedBy: String): Shipping {
        val shipping = shippingRepository.findByTrackingNumber(trackingNumber)
            ?: error("Shipping not found for trackingNumber=$trackingNumber")

        val updated = shippingRepository.updateDelivered(shipping, receivedBy)

        val order = orderServicePort.findOrderById(shipping.orderId)
            ?: error("Order not found for orderId=${shipping.orderId}")

        orderActionPublisher.publish(order.copy(status = "ORDER_DELIVERED"))

        return updated
    }

    fun registerDeliveryFailure(trackingNumber: String, failureReason: String): Shipping {
        val shipping = shippingRepository.findByTrackingNumber(trackingNumber)
            ?: error("Shipping not found for trackingNumber=$trackingNumber")

        val updated = shippingRepository.updateFailed(shipping, failureReason)

        val order = orderServicePort.findOrderById(shipping.orderId)
            ?: error("Order not found for orderId=${shipping.orderId}")

        orderActionPublisher.publish(order.copy(status = "ORDER_NOT_DELIVERED"))

        return updated
    }
}

