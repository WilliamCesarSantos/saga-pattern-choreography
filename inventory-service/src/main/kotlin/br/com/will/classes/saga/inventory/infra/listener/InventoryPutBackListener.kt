package br.com.will.classes.saga.inventory.infra.listener

import br.com.will.classes.saga.inventory.usecases.InventoryUseCase
import br.com.will.classes.saga.shared.model.Order
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InventoryPutBackListener(
    private val inventoryUseCase: InventoryUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener($$"${inventory-service.sqs.put-back.queue-name}")
    fun listen(order: Order) {
        log.info("[Inventory] Received put-back message — orderId=${order.orderId}")
        inventoryUseCase.revertWriteOff(order)
    }
}

