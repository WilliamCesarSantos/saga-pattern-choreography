package br.com.will.classes.saga.order.infra.message

import br.com.will.classes.saga.order.domain.model.Order
import org.springframework.stereotype.Component

@Component
class OrderEventProducer {
    // Em uma implementação real, isso injetaria um KafkaTemplate, RabbitTemplate, etc.
    
    fun publishOrderFinalized(order: Order) {
        // Implementação simplificada para demonstração
        println("Mensagem publicada no broker: Pedido ${order.id} foi finalizado para o cliente ${order.customerId}")
        
        // Aqui você publicaria a mensagem real em um broker como Kafka ou RabbitMQ
        // Por exemplo:
        // kafkaTemplate.send("order-finalized-topic", order.id, orderMapper.toEvent(order))
    }
}