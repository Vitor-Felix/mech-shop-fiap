package com.vitfelix.mech_shop.infrastructure.client

import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

@Component
class VehicleSalesClient(
    private val restClient: RestClient,
    @Value("\${vehicle-sales.base-url}") private val baseUrl: String,
    @Value("\${MECH_SHOP_INTERNAL_TOKEN:}") private val token: String
) {
    fun synchronize(vehicleId: Long, request: VehicleSyncRequest) {
        if (baseUrl.isBlank()) {
            throw VehicleSyncException("URL do serviço de vendas não configurada")
        }
        if (token.isBlank()) {
            throw VehicleSyncException("Token interno do serviço de vendas não configurado")
        }

        try {
            val response = restClient.put()
                .uri("${baseUrl.trimEnd('/')}/internal/vehicles/$vehicleId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity()
            if (response.statusCode.value() !in 200..201) {
                throw VehicleSyncException(
                    "Serviço de vendas respondeu HTTP ${response.statusCode.value()}"
                )
            }
        } catch (exception: RestClientResponseException) {
            throw VehicleSyncException("Serviço de vendas respondeu HTTP ${exception.statusCode.value()}")
        } catch (exception: RestClientException) {
            throw VehicleSyncException("Falha de comunicação com o serviço de vendas")
        }
    }
}
