package br.com.will.classes.saga.payment.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Configuration
class AwsConfig(
    @param:Value("\${cloud.aws.region.static}") private val region: String,
    @param:Value("\${cloud.aws.credentials.access-key}") private val accessKey: String,
    @param:Value("\${cloud.aws.credentials.secret-key}") private val secretKey: String,
    @param:Value("\${aws.endpoint}") private val endpoint: String
) {

    private fun credentials() =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))

    @Bean
    fun snsClient(): SnsClient = SnsClient.builder()
        .region(Region.of(region))
        .credentialsProvider(credentials())
        .endpointOverride(URI.create(endpoint))
        .build()

    @Bean
    fun sqsClient(): SqsClient = SqsClient.builder()
        .region(Region.of(region))
        .credentialsProvider(credentials())
        .endpointOverride(URI.create(endpoint))
        .build()

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}

