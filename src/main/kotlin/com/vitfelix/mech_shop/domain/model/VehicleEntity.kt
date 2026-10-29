package com.vitfelix.mech_shop.domain.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
data class VehicleEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(nullable = true)
    val licensePlate: String? = null,
    val brand: String,
    val model: String,
    @Column(name = "vehicle_year")
    val year: Int,
    @ManyToOne(optional = true)
    val customer: CustomerEntity? = null,
    val color: String = "",
    @Column(precision = 12, scale = 2)
    val price: BigDecimal = BigDecimal.ZERO,
    var version: Long = 1,
)
