package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.application.dto.response.VehicleResponse
import com.vitfelix.mech_shop.domain.model.VehicleEntity
import com.vitfelix.mech_shop.domain.repository.VehicleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository
) {
    fun getAllVehicles() = vehicleRepository.findAll().map { it.toResponse() }

    fun getVehicleById(id: Long) = findVehicleById(id).toResponse()

    fun createVehicle(dto: VehicleRequest): VehicleResponse {
        val vehicle = VehicleEntity(
            brand = dto.brand,
            model = dto.model,
            year = dto.year,
            color = dto.color,
            price = dto.price
        )
        return vehicleRepository.save(vehicle).toResponse()
    }

    fun updateVehicle(id: Long, dto: VehicleRequest): VehicleResponse {
        val existing = findVehicleById(id)
        return vehicleRepository.save(
            existing.copy(
                brand = dto.brand,
                model = dto.model,
                year = dto.year,
                color = dto.color,
                price = dto.price,
                version = existing.version + 1
            )
        ).toResponse()
    }

    fun deleteVehicle(id: Long) = vehicleRepository.deleteById(id)

    private fun findVehicleById(id: Long) = vehicleRepository.findByIdOrNull(id)
        ?: throw RuntimeException("Vehicle not found")

    private fun VehicleEntity.toResponse() = VehicleResponse(
        id = id,
        brand = brand,
        model = model,
        year = year,
        color = color,
        price = price.setScale(2),
        version = version
    )
}
