package br.com.will.classes.saga.notification.application.port

interface EmailSenderPort {
    fun send(to: String, subject: String, body: String)
}

