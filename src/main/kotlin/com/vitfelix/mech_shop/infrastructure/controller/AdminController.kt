package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.service.AdminService
import com.vitfelix.mech_shop.domain.model.PartEntity
import com.vitfelix.mech_shop.domain.model.WorkEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.vitfelix.mech_shop.application.service.WorkOrderService

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService,
    private val workOrderService: WorkOrderService
) {

    @PostMapping("/services")
    fun createWork(@RequestBody work: WorkEntity): ResponseEntity<WorkEntity> =
        ResponseEntity.status(HttpStatus.CREATED).body(adminService.createWork(work))

    @GetMapping("/services")
    fun getAllWorks(): ResponseEntity<List<WorkEntity>> =
        ResponseEntity.ok(adminService.getAllWorks())

    @PostMapping("/parts")
    fun createPart(@RequestBody part: PartEntity): ResponseEntity<PartEntity> =
        ResponseEntity.status(HttpStatus.CREATED).body(adminService.createPart(part))

    @GetMapping("/parts")
    fun getAllParts(): ResponseEntity<List<PartEntity>> =
        ResponseEntity.ok(adminService.getAllParts())

    @GetMapping("/metrics/average-time")
    fun getAverageExecutionTime(): ResponseEntity<Map<String, Any>> {
        val avgTime = workOrderService.getAverageExecutionTimeInMinutes()
        return ResponseEntity.ok(mapOf("averageExecutionTimeInMinutes" to avgTime))
    }
}
