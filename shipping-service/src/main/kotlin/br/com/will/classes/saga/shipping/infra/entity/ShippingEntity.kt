package br.com.will.classes.saga.shipping.infra.entity

import br.com.will.classes.saga.shipping.domain.model.Shipping
import br.com.will.classes.saga.shipping.domain.model.ShippingStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "shipping")
class ShippingEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Column(name = "customer_name", nullable = false)
    val customerName: String,

    @Column(name = "customer_email", nullable = false)
    val customerEmail: String,

    @Column(name = "tracking_number", nullable = false, unique = true)
    val trackingNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ShippingStatus,

    @Column(name = "received_by")
    val receivedBy: String? = null,

    @Column(name = "failure_reason")
    val failureReason: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Shipping = Shipping(
        id = id,
        orderId = orderId,
        customerName = customerName,
        customerEmail = customerEmail,
        trackingNumber = trackingNumber,
        status = status,
        receivedBy = receivedBy,
        failureReason = failureReason,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(shipping: Shipping): ShippingEntity = ShippingEntity(
            id = shipping.id,
            orderId = shipping.orderId,
            customerName = shipping.customerName,
            customerEmail = shipping.customerEmail,
            trackingNumber = shipping.trackingNumber,
            status = shipping.status,
            receivedBy = shipping.receivedBy,
            failureReason = shipping.failureReason,
            createdAt = shipping.createdAt,
            updatedAt = shipping.updatedAt
        )
    }
}

