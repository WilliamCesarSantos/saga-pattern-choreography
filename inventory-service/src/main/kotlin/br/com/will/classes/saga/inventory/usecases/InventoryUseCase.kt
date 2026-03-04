package br.com.will.classes.saga.inventory.usecases

import br.com.will.classes.saga.inventory.domain.exception.OutOfStockException
import br.com.will.classes.saga.inventory.domain.repository.InventoryRepository
import br.com.will.classes.saga.shared.dto.OrderDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InventoryUseCase(
    private val inventoryRepository: InventoryRepository
) {

    @Transactional
    fun processWriteOff(orderDTO: OrderDTO) {
        orderDTO.items.forEach { item ->
            val productId = UUID.fromString(item.product.id)
            val inventory = inventoryRepository.findByProductId(productId)
                ?: throw OutOfStockException(productId)

            if (inventory.quantity < item.quantity) {
                throw OutOfStockException(productId)
            }
        }

        orderDTO.items.forEach { item ->
            val productId = UUID.fromString(item.product.id)
            val inventory = inventoryRepository.findByProductId(productId)!!
            inventory.quantity -= item.quantity
            inventoryRepository.save(inventory)
        }
    }
}

