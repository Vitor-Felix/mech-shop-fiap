package com.vitfelix.mech_shop.application.dto.request

data class CreateWorkOrderRequest(
    val vehicleId: Long,
    val observation: String?
)
