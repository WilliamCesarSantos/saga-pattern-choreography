package br.com.will.classes.saga.order.infra.entity

import br.com.will.classes.saga.shared.model.Customer
import jakarta.persistence.*

@Entity
@Table(name = "customers")
class CustomerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    val email: String = ""
) {
    fun toDomain(): Customer = Customer(id = id, name = name, email = email)
}
