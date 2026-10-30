package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import com.vitfelix.mech_shop.application.dto.response.VehicleResponse
import com.vitfelix.mech_shop.domain.enums.VehicleSyncStatus
import com.vitfelix.mech_shop.domain.model.VehicleEntity
import com.vitfelix.mech_shop.domain.repository.VehicleRepository
import com.vitfelix.mech_shop.infrastructure.client.VehicleSalesClient
import com.vitfelix.mech_shop.infrastructure.client.VehicleSyncException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val vehicleSalesClient: VehicleSalesClient
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
        val saved = vehicleRepository.save(vehicle)
        return synchronize(saved)
    }

    fun updateVehicle(id: Long, dto: VehicleRequest): VehicleResponse {
        val existing = findVehicleById(id)
        val saved = vehicleRepository.save(
            existing.copy(
                brand = dto.brand,
                model = dto.model,
                year = dto.year,
                color = dto.color,
                price = dto.price,
                version = existing.version + 1,
                syncStatus = VehicleSyncStatus.PENDING,
                syncError = null
            )
        )
        return synchronize(saved)
    }

    fun retrySynchronization(id: Long): VehicleResponse {
        val vehicle = findVehicleById(id)
        val pending = vehicleRepository.save(
            vehicle.copy(syncStatus = VehicleSyncStatus.PENDING, syncError = null)
        )
        return synchronize(pending)
    }

    fun deleteVehicle(id: Long) = vehicleRepository.deleteById(id)

    private fun findVehicleById(id: Long) = vehicleRepository.findByIdOrNull(id)
        ?: throw RuntimeException("Vehicle not found")

    private fun synchronize(vehicle: VehicleEntity): VehicleResponse {
        val vehicleId = requireNotNull(vehicle.id) { "Persisted vehicle must have an ID" }
        val syncRequest = VehicleSyncRequest(
            brand = vehicle.brand,
            model = vehicle.model,
            year = vehicle.year,
            color = vehicle.color,
            price = vehicle.price,
            version = vehicle.version
        )
        val (status, error) = try {
            vehicleSalesClient.synchronize(vehicleId, syncRequest)
            VehicleSyncStatus.SYNCED to null
        } catch (exception: VehicleSyncException) {
            VehicleSyncStatus.FAILED to exception.message
        }

        val current = findVehicleById(vehicleId)
        if (current.version != vehicle.version) {
            return current.toResponse()
        }
        return vehicleRepository.save(
            current.copy(syncStatus = status, syncError = error)
        ).toResponse()
    }

    private fun VehicleEntity.toResponse() = VehicleResponse(
        id = id,
        brand = brand,
        model = model,
        year = year,
        color = color,
        price = price.setScale(2),
        version = version,
        syncStatus = syncStatus,
        syncError = syncError
    )
}
