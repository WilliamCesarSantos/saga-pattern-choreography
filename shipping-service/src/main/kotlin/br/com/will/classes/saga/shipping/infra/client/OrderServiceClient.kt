package br.com.will.classes.saga.shipping.infra.client

import br.com.will.classes.saga.shared.dto.OrderDTO
import br.com.will.classes.saga.shipping.domain.port.OrderServicePort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class OrderServiceClient(
    @param:Value($$"${shipping-service.rest.order-service.base-url}")
    private val baseUrl: String
) : OrderServicePort {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    override fun findOrderById(orderId: Long): OrderDTO? =
        webClient.get()
            .uri("/orders/{orderId}", orderId)
            .retrieve()
            .bodyToMono(OrderDTO::class.java)
            .block()
}
