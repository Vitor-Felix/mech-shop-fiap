package com.vitfelix.mech_shop.infrastructure.client

import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.io.IOException
import java.math.BigDecimal

class VehicleSalesClientTest {
    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val client = VehicleSalesClient(
        builder.build(),
        "http://sales.test",
        "test-only-token"
    )
    private val request = VehicleSyncRequest(
        brand = "Toyota",
        model = "Corolla",
        year = 2024,
        color = "Prata",
        price = BigDecimal("125000.00"),
        version = 1L
    )

    @Test
    fun `should send exact versioned payload and bearer token`() {
        server.expect(once(), requestTo("http://sales.test/internal/vehicles/42"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-only-token"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                content().json(
                    """{"brand":"Toyota","model":"Corolla","year":2024,"color":"Prata","price":125000.00,"version":1}""",
                    true
                )
            )
            .andRespond(withSuccess())

        client.synchronize(42L, request)

        server.verify()
    }

    @Test
    fun `should report remote server errors without exposing response body`() {
        server.expect(requestTo("http://sales.test/internal/vehicles/42"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).body("sensitive upstream response"))

        val exception = assertThrows(VehicleSyncException::class.java) {
            client.synchronize(42L, request)
        }

        assertEquals("Serviço de vendas respondeu HTTP 503", exception.message)
        server.verify()
    }

    @Test
    fun `should not treat statuses outside the contract as successful`() {
        server.expect(requestTo("http://sales.test/internal/vehicles/42"))
            .andRespond(withStatus(HttpStatus.NO_CONTENT))

        val exception = assertThrows(VehicleSyncException::class.java) {
            client.synchronize(42L, request)
        }

        assertEquals("Serviço de vendas respondeu HTTP 204", exception.message)
        server.verify()
    }

    @Test
    fun `should report communication failures with a sanitized message`() {
        server.expect(requestTo("http://sales.test/internal/vehicles/42"))
            .andRespond { throw IOException("request timed out with test token") }

        val exception = assertThrows(VehicleSyncException::class.java) {
            client.synchronize(42L, request)
        }

        assertEquals("Falha de comunicação com o serviço de vendas", exception.message)
        server.verify()
    }

    @Test
    fun `should report missing configuration without making a request`() {
        val unconfiguredClient = VehicleSalesClient(RestClient.builder().build(), "", "")

        val exception = assertThrows(VehicleSyncException::class.java) {
            unconfiguredClient.synchronize(42L, request)
        }

        assertEquals("URL do serviço de vendas não configurada", exception.message)
        server.verify()
    }
}
