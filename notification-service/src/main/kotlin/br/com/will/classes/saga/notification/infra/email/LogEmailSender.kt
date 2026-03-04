package br.com.will.classes.saga.notification.infra.email

import br.com.will.classes.saga.notification.application.port.EmailSenderPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogEmailSender : EmailSenderPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun send(to: String, subject: String, body: String) {
        logger.info("[EMAIL SIMULADO] Para: $to | Assunto: $subject | Corpo: $body")
    }
}

