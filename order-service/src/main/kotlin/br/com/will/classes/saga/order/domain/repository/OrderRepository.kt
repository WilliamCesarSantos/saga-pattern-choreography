package br.com.will.classes.saga.order.domain.repository

import br.com.will.classes.saga.order.domain.model.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, String>
