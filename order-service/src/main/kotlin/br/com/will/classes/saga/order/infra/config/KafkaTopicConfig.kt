package br.com.will.classes.saga.order.infra.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {
    
    @Value("\${kafka.topic.order-finalized}")
    private lateinit var orderFinalizedTopic: String
    
    @Bean
    fun orderFinalizedTopic(): NewTopic {
        return TopicBuilder.name(orderFinalizedTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }
}