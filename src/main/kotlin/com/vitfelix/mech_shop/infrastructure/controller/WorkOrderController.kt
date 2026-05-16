package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.dto.CreateWorkOrderDto
import com.vitfelix.mech_shop.application.service.WorkOrderService
import com.vitfelix.mech_shop.domain.model.WorkOrderEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/work-orders")
class WorkOrderController(
    private val workOrderService: WorkOrderService
) {

    @PostMapping
    fun createWorkOrder(@RequestBody dto: CreateWorkOrderDto): ResponseEntity<WorkOrderEntity> {
        val createdOrder = workOrderService.createWorkOrder(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder)
    }
}
