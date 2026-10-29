package com.vitfelix.mech_shop.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.math.BigDecimal

data class VehicleRequest(
    @field:NotBlank(message = "A marca é obrigatória")
    val brand: String,

    @field:NotBlank(message = "O modelo é obrigatório")
    val model: String,

    @field:Min(value = 1886, message = "O ano deve ser 1886 ou posterior")
    @field:Max(value = 2100, message = "O ano deve ser 2100 ou anterior")
    val year: Int,

    @field:NotBlank(message = "A cor é obrigatória")
    val color: String,

    @field:DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @field:Digits(integer = 10, fraction = 2, message = "O preço deve ter no máximo duas casas decimais")
    val price: BigDecimal
)
