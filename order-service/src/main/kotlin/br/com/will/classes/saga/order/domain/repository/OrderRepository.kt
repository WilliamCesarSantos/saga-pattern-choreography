package br.com.will.classes.saga.order.domain.repository

import br.com.will.classes.saga.order.domain.model.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

// TODO criar um entity para o order, para trabalhar a camada de persistencia.
@Repository
interface OrderRepository : JpaRepository<Order, String>
