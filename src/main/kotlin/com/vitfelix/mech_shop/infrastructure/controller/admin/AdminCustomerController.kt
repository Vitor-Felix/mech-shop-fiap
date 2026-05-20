package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.service.CustomerService
import com.vitfelix.mech_shop.domain.model.CustomerEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/customers")
class AdminCustomerController(private val service: CustomerService) {
    @GetMapping
    fun getAll() = service.getAllCustomers()
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = service.getCustomerById(id)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody customer: CustomerEntity) = service.createCustomer(customer)
    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody customer: CustomerEntity) = service.updateCustomer(id, customer)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.deleteCustomer(id)
}
