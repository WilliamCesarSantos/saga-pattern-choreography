package br.com.will.classes.saga.inventory.domain.exception

import java.util.UUID

class OutOfStockException(productId: UUID) :
    RuntimeException("Product $productId is out of stock")

