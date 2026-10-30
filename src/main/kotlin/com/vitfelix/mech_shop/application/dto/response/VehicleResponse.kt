package com.vitfelix.mech_shop.application.dto.response

import com.vitfelix.mech_shop.domain.enums.VehicleSyncStatus
import java.math.BigDecimal

data class VehicleResponse(
    val id: Long?,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    val price: BigDecimal,
    val version: Long,
    val syncStatus: VehicleSyncStatus = VehicleSyncStatus.PENDING,
    val syncError: String? = null
)
