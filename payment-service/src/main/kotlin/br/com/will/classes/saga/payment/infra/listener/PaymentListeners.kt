package br.com.will.classes.saga.payment.infra.listener

import br.com.will.classes.saga.payment.dto.OrderDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Component
class PaymentPoller(private val sqsClient: SqsClient, private val snsClient: SnsClient) {
    private val mapper = jacksonObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)
    private val checkoutQueue = "PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE"
    private val revertQueue = "PAYMENT_SERVICE_ORDER_REVERT_QUEUE"
    private val topicArn = System.getenv("ORDER_ACTION_TOPIC_ARN") ?: "arn:aws:sns:us-east-1:000000000000:ORDER_ACTION"

    @Scheduled(fixedDelayString = "5000")
    fun pollCheckout() {
        try {
            val req = ReceiveMessageRequest.builder().queueUrl(queueUrl(checkoutQueue)).maxNumberOfMessages(5).waitTimeSeconds(1).build()
            val resp = sqsClient.receiveMessage(req)
            resp.messages().forEach { msg ->
                try {
                    val order = mapper.readValue(msg.body(), OrderDTO::class.java)
                    log.info("[Payment] Processing checkout for order=${order.orderId} status=${order.status}")
                    val total = order.items.sumOf { it.price * it.quantity }
                    val updated = if ((total.toInt() % 2) == 0) order.copy(status = "ORDER_PAID") else order.copy(status = "PAYMENT_FAILED")
                    publishOrder(updated)
                } catch (ex: Exception) {
                    log.error("[Payment] error processing message", ex)
                } finally {
                    sqsClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl(checkoutQueue)).receiptHandle(msg.receiptHandle()).build())
                }
            }
        } catch (ex: Exception) {
            log.error("[Payment] pollCheckout outer error", ex)
        }
    }

    @Scheduled(fixedDelayString = "7000")
    fun pollRevert() {
        try {
            val req = ReceiveMessageRequest.builder().queueUrl(queueUrl(revertQueue)).maxNumberOfMessages(5).waitTimeSeconds(1).build()
            val resp = sqsClient.receiveMessage(req)
            resp.messages().forEach { msg ->
                try {
                    val order = mapper.readValue(msg.body(), OrderDTO::class.java)
                    log.info("[Payment] Processing revert for order=${order.orderId} status=${order.status}")
                    val updated = order.copy(status = "ORDER_PAID_REVERSED")
                    publishOrder(updated)
                } catch (ex: Exception) {
                    log.error("[Payment] error processing revert message", ex)
                } finally {
                    sqsClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl(revertQueue)).receiptHandle(msg.receiptHandle()).build())
                }
            }
        } catch (ex: Exception) {
            log.error("[Payment] pollRevert outer error", ex)
        }
    }

    private fun publishOrder(order: OrderDTO) {
        val body = mapper.writeValueAsString(order)
        val request = PublishRequest.builder()
            .topicArn(topicArn)
            .message(body)
            .messageAttributes(mapOf("status" to MessageAttributeValue.builder().dataType("String").stringValue(order.status).build()))
            .build()
        snsClient.publish(request)
    }

    private fun queueUrl(name: String): String = name
}
