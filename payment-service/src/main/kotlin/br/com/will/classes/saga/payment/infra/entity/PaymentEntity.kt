package br.com.will.classes.saga.payment.infra.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "payments")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "order_id", nullable = false)
    val orderId: Long = 0,

    @Column(nullable = false)
    var status: String = "",

    @Column(nullable = false)
    val amount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "transaction_id", nullable = false)
    val transactionId: String = "",

    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant = Instant.now()
)

