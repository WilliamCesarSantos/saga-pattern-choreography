package br.com.will.classes.saga.inventory.domain.exception

class OutOfStockException(productId: Long) :
    RuntimeException("Product $productId is out of stock")
