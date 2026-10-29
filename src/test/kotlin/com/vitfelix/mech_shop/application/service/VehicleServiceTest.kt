package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.domain.model.VehicleEntity
import com.vitfelix.mech_shop.domain.repository.VehicleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.Optional

class VehicleServiceTest {

    private val vehicleRepository: VehicleRepository = mock(VehicleRepository::class.java)
    private val vehicleService = VehicleService(vehicleRepository)
    private val request = VehicleRequest(
        brand = "Ford",
        model = "Fiesta",
        year = 2020,
        color = "Azul",
        price = BigDecimal("45000.00")
    )

    @Test
    fun `should return all vehicles without legacy workshop fields`() {
        val vehicle = vehicle(id = 1L)
        `when`(vehicleRepository.findAll()).thenReturn(listOf(vehicle))

        val result = vehicleService.getAllVehicles()

        assertEquals(1, result.size)
        assertEquals("Ford", result[0].brand)
        assertEquals("Azul", result[0].color)
        assertEquals(BigDecimal("45000.00"), result[0].price)
        assertEquals(1L, result[0].version)
    }

    @Test
    fun `should return vehicle by id when exists`() {
        `when`(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle(id = 1L)))

        val result = vehicleService.getVehicleById(1L)

        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("Fiesta", result.model)
    }

    @Test
    fun `should throw exception when vehicle id does not exist`() {
        `when`(vehicleRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows(RuntimeException::class.java) {
            vehicleService.getVehicleById(1L)
        }

        assertEquals("Vehicle not found", exception.message)
    }

    @Test
    fun `should create vehicle with initial version and no workshop customer`() {
        val captor = ArgumentCaptor.forClass(VehicleEntity::class.java)
        `when`(vehicleRepository.save(any(VehicleEntity::class.java))).thenAnswer { invocation ->
            invocation.getArgument<VehicleEntity>(0).copy(id = 1L)
        }

        val result = vehicleService.createVehicle(request)

        verify(vehicleRepository).save(captor.capture())
        val persisted = captor.value
        assertEquals("Ford", persisted.brand)
        assertEquals("Fiesta", persisted.model)
        assertEquals(2020, persisted.year)
        assertEquals("Azul", persisted.color)
        assertEquals(BigDecimal("45000.00"), persisted.price)
        assertEquals(1L, persisted.version)
        assertEquals(null, persisted.licensePlate)
        assertEquals(null, persisted.customer)
        assertEquals(1L, result.id)
    }

    @Test
    fun `should update vehicle fields and increment version`() {
        val existing = vehicle(id = 1L, version = 4L)
        val captor = ArgumentCaptor.forClass(VehicleEntity::class.java)
        `when`(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing))
        `when`(vehicleRepository.save(any(VehicleEntity::class.java))).thenAnswer { invocation ->
            invocation.getArgument<VehicleEntity>(0)
        }

        val result = vehicleService.updateVehicle(
            1L,
            request.copy(model = "Polo", color = "Prata", price = BigDecimal("52000.00"))
        )

        verify(vehicleRepository).save(captor.capture())
        assertEquals("Polo", captor.value.model)
        assertEquals("Prata", captor.value.color)
        assertEquals(BigDecimal("52000.00"), captor.value.price)
        assertEquals(5L, captor.value.version)
        assertEquals(5L, result.version)
    }

    @Test
    fun `should delete vehicle successfully`() {
        doNothing().`when`(vehicleRepository).deleteById(1L)

        vehicleService.deleteVehicle(1L)

        verify(vehicleRepository, times(1)).deleteById(1L)
    }

    private fun vehicle(id: Long, version: Long = 1L) = VehicleEntity(
        id = id,
        brand = "Ford",
        model = "Fiesta",
        year = 2020,
        color = "Azul",
        price = BigDecimal("45000.00"),
        version = version
    )
}
