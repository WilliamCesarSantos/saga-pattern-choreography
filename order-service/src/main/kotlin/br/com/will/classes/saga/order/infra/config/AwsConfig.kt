package br.com.will.classes.saga.order.infra.config

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
    @Value("\${aws.endpoint:http://localhost:4566}")
    private val awsEndpoint: String
) {

    private fun credentials() = StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"))

    @Bean
    fun snsClient(): SnsClient = SnsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(credentials())
        .endpointOverride(URI.create(awsEndpoint))
        .build()

    @Bean
    fun sqsClient(): SqsClient = SqsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(credentials())
        .endpointOverride(URI.create(awsEndpoint))
        .build()
}

