package com.vitfelix.mech_shop.domain.model

import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
data class WorkOrderEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @ManyToOne val customer: CustomerEntity,
    @ManyToOne val vehicle: VehicleEntity,
    @Enumerated(EnumType.STRING) var status: WorkOrderStatusEnum = WorkOrderStatusEnum.RECEIVED,
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @ManyToMany
    @JoinTable(
        name = "work_order_service",
        joinColumns = [JoinColumn(name = "work_order_id")],
        inverseJoinColumns = [JoinColumn(name = "service_id")]
    )
    val works: MutableList<WorkEntity> = mutableListOf(),

    @ManyToMany
    @JoinTable(
        name = "work_order_part",
        joinColumns = [JoinColumn(name = "work_order_id")],
        inverseJoinColumns = [JoinColumn(name = "part_id")]
    )
    val parts: MutableList<PartEntity> = mutableListOf()
) {
    // Domain rule: Budget generated automatically
    fun calculateBudget() {
        val totalServices = works.sumOf { it.amount }
        val totalParts = parts.sumOf { it.unitPrice }
        this.totalAmount = totalServices + totalParts
    }
}
