package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.application.dto.response.VehicleResponse
import com.vitfelix.mech_shop.application.service.VehicleService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal

@SpringBootTest
class AdminVehicleControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var vehicleService: VehicleService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    private val sampleRequest = VehicleRequest(
        brand = "Ford",
        model = "Fiesta",
        year = 2020,
        color = "Azul",
        price = BigDecimal("45000.00")
    )

    private val sampleEntity = VehicleResponse(
        id = 1L,
        brand = "Ford",
        model = "Fiesta",
        year = 2020,
        color = "Azul",
        price = BigDecimal("45000.00"),
        version = 1L
    )

    @Test
    @WithMockUser
    fun `should return all vehicles`() {
        `when`(vehicleService.getAllVehicles()).thenReturn(listOf(sampleEntity))

        mockMvc.perform(get("/api/admin/vehicles")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].color").value("Azul"))
            .andExpect(jsonPath("$[0].price").value(45000.0))
            .andExpect(jsonPath("$[0].licensePlate").doesNotExist())
            .andExpect(jsonPath("$[0].customer").doesNotExist())
    }

    @Test
    @WithMockUser
    fun `should return vehicle by id`() {
        `when`(vehicleService.getVehicleById(1L)).thenReturn(sampleEntity)

        mockMvc.perform(get("/api/admin/vehicles/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.model").value("Fiesta"))
    }

    @Test
    @WithMockUser
    fun `should create new vehicle`() {
        `when`(vehicleService.createVehicle(sampleRequest)).thenReturn(sampleEntity)

        mockMvc.perform(post("/api/admin/vehicles")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.color").value("Azul"))
            .andExpect(jsonPath("$.price").value(45000.0))
            .andExpect(jsonPath("$.licensePlate").doesNotExist())
            .andExpect(jsonPath("$.customer").doesNotExist())
    }

    @Test
    @WithMockUser
    fun `should reject invalid vehicle data`() {
        val invalidRequest = sampleRequest.copy(price = BigDecimal("-1.00"))

        mockMvc.perform(post("/api/admin/vehicles")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `should update existing vehicle`() {
        `when`(vehicleService.updateVehicle(1L, sampleRequest)).thenReturn(sampleEntity)

        mockMvc.perform(put("/api/admin/vehicles/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    @WithMockUser
    fun `should delete vehicle by id`() {
        mockMvc.perform(delete("/api/admin/vehicles/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)

        verify(vehicleService).deleteVehicle(1L)
    }
}
