package br.com.will.classes.saga.shipping.infra.controller

import br.com.will.classes.saga.shipping.usecases.ShippingService
import br.com.will.classes.saga.shipping.domain.model.Shipping
import br.com.will.classes.saga.shipping.infra.dto.DeliveryUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shippings")
class ShippingController(
    private val shippingService: ShippingService
) {

    @PutMapping("/{trackingNumber}/delivery")
    fun updateDelivery(
        @PathVariable trackingNumber: String,
        @RequestBody request: DeliveryUpdateRequest
    ): ResponseEntity<Shipping> {
        val result = if (request.success) {
            val receivedBy = requireNotNull(request.receivedBy) {
                "receivedBy is required for a successful delivery"
            }
            shippingService.confirmDelivery(trackingNumber, receivedBy)
        } else {
            val failureReason = requireNotNull(request.failureReason) {
                "failureReason is required for a failed delivery"
            }
            shippingService.registerDeliveryFailure(trackingNumber, failureReason)
        }

        return ResponseEntity.ok(result)
    }
}

