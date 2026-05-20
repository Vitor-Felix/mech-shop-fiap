package com.vitfelix.mech_shop.application.dto.request

data class VehicleRequest(
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val customerId: Long
)
