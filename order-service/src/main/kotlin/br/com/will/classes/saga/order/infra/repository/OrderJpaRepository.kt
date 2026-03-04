package br.com.will.classes.saga.order.infra.repository

import br.com.will.classes.saga.order.infra.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderEntity, Long>
