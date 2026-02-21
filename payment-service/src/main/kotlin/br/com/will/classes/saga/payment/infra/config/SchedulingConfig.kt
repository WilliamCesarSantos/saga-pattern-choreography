package br.com.will.classes.saga.payment.infra.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class SchedulingConfig {
    // enables @Scheduled methods in the application
}

