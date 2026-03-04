package br.com.will.classes.saga.inventory.infra.listener

import br.com.will.classes.saga.inventory.domain.exception.OutOfStockException
import br.com.will.classes.saga.inventory.domain.port.InventoryEventPublisher
import br.com.will.classes.saga.inventory.usecases.InventoryUseCase
import br.com.will.classes.saga.shared.dto.OrderDTO
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InventoryWriteOffListener(
    private val inventoryUseCase: InventoryUseCase,
    private val eventPublisher: InventoryEventPublisher,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${inventory-service.sqs.write-off.queue-name}")
    fun listen(message: String) {
        val orderDTO = objectMapper.readValue(message, OrderDTO::class.java)
        log.info("[Inventory] Received message — orderId=${orderDTO.orderId}")
        try {
            inventoryUseCase.processWriteOff(orderDTO)
            val updated = orderDTO.copy(status = "INVENTORY_WRITE_OFF")
            eventPublisher.publish(updated)
            log.info("[Inventory] Write-off completed — orderId=${orderDTO.orderId}")
        } catch (e: OutOfStockException) {
            log.warn("[Inventory] Out of stock — orderId=${orderDTO.orderId} reason=${e.message}")
            val updated = orderDTO.copy(status = "OUT_OF_STOCK")
            eventPublisher.publish(updated)
        }
    }
}

