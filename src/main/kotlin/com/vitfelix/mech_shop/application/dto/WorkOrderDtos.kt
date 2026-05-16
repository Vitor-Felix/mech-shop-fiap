package com.vitfelix.mech_shop.application.dto

data class CreateWorkOrderDto(
    val customerDocument: String,
    val customerName: String,
    val vehicleLicensePlate: String,
    val vehicleBrand: String,
    val vehicleModel: String,
    val vehicleYear: Int,
    val serviceIds: List<Long>,
    val partIds: List<Long>
)
