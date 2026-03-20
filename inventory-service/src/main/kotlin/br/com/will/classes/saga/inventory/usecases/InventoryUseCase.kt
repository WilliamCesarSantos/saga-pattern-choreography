package br.com.will.classes.saga.inventory.usecases

import br.com.will.classes.saga.inventory.domain.exception.OutOfStockException
import br.com.will.classes.saga.inventory.domain.port.InventoryEventPublisher
import br.com.will.classes.saga.inventory.domain.repository.InventoryRepository
import br.com.will.classes.saga.shared.model.Order
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryUseCase(
    private val inventoryRepository: InventoryRepository,
    private val eventPublisher: InventoryEventPublisher
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun processWriteOff(order: Order) {
        try {
            writeOff(order)
            val updated = order.copy(status = "INVENTORY_WRITE_OFF")
            eventPublisher.publish(updated)
            log.info("[Inventory] Write-off completed — orderId=${order.orderId}")
        } catch (e: OutOfStockException) {
            log.warn("[Inventory] Out of stock — orderId=${order.orderId} reason=${e.message}")
            val updated = order.copy(status = "OUT_OF_STOCK")
            eventPublisher.publish(updated)
        }
    }

    @Transactional
    fun revertWriteOff(order: Order) {
        order.items.forEach { item ->
            val productId = item.product.id
            val inventory = inventoryRepository.findByProductId(productId)
            if (inventory == null) {
                log.warn("[Inventory] Product $productId not found for revert, skipping")
                return@forEach
            }
            inventory.quantity += item.quantity
            inventoryRepository.save(inventory)
        }
        log.info("[Inventory] Stock reverted — orderId=${order.orderId}")
    }

    private fun writeOff(order: Order) {
        order.items.forEach { item ->
            val productId = item.product.id
            val inventory = inventoryRepository.findByProductId(productId)
                ?: throw OutOfStockException(productId)

            if (inventory.quantity < item.quantity) {
                throw OutOfStockException(productId)
            } else {
                inventory.quantity -= item.quantity
                inventoryRepository.save(inventory)
            }
        }
    }
}
