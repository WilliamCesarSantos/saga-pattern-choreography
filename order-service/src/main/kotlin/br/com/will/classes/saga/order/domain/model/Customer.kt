package br.com.will.classes.saga.order.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "customers")
class Customer(
    @Id
    val id: String,
    val name: String,
    val email: String
)

