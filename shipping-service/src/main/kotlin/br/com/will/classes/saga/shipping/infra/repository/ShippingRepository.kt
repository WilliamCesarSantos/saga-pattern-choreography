package br.com.will.classes.saga.shipping.infra.repository

import br.com.will.classes.saga.shipping.domain.model.Shipping
import br.com.will.classes.saga.shipping.domain.model.ShippingStatus
import br.com.will.classes.saga.shipping.infra.entity.ShippingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

interface ShippingJpaRepository : JpaRepository<ShippingEntity, Long> {
    fun findByTrackingNumber(trackingNumber: String): ShippingEntity?
    fun findByOrderId(orderId: Long): ShippingEntity?
}

@Repository
class ShippingRepository(private val jpaRepository: ShippingJpaRepository) {

    fun save(shipping: Shipping): Shipping =
        jpaRepository.save(ShippingEntity.fromDomain(shipping)).toDomain()

    fun findByTrackingNumber(trackingNumber: String): Shipping? =
        jpaRepository.findByTrackingNumber(trackingNumber)?.toDomain()

    fun findByOrderId(orderId: Long): Shipping? =
        jpaRepository.findByOrderId(orderId)?.toDomain()

    fun updateDelivered(shipping: Shipping, receivedBy: String): Shipping {
        val updated = ShippingEntity(
            id = shipping.id,
            orderId = shipping.orderId,
            customerName = shipping.customerName,
            customerEmail = shipping.customerEmail,
            trackingNumber = shipping.trackingNumber,
            status = ShippingStatus.DELIVERED,
            receivedBy = receivedBy,
            failureReason = null,
            createdAt = shipping.createdAt,
            updatedAt = LocalDateTime.now()
        )
        return jpaRepository.save(updated).toDomain()
    }

    fun updateFailed(shipping: Shipping, failureReason: String): Shipping {
        val updated = ShippingEntity(
            id = shipping.id,
            orderId = shipping.orderId,
            customerName = shipping.customerName,
            customerEmail = shipping.customerEmail,
            trackingNumber = shipping.trackingNumber,
            status = ShippingStatus.FAILED,
            receivedBy = null,
            failureReason = failureReason,
            createdAt = shipping.createdAt,
            updatedAt = LocalDateTime.now()
        )
        return jpaRepository.save(updated).toDomain()
    }
}

