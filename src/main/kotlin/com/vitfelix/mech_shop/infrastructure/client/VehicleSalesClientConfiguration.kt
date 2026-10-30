package com.vitfelix.mech_shop.infrastructure.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class VehicleSalesClientConfiguration {
    @Bean
    fun vehicleSalesRestClient(
        @Value("\${vehicle-sales.connect-timeout}") connectTimeout: Duration,
        @Value("\${vehicle-sales.read-timeout}") readTimeout: Duration
    ): RestClient {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient)
            .apply { setReadTimeout(readTimeout) }

        return RestClient.builder()
            .requestFactory(requestFactory)
            .build()
    }
}
