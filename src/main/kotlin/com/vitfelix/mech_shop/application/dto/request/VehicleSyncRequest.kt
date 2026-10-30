package com.vitfelix.mech_shop.application.dto.request

import java.math.BigDecimal

data class VehicleSyncRequest(
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    val price: BigDecimal,
    val version: Long
)
