package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.domain.model.CustomerEntity
import com.vitfelix.mech_shop.domain.repository.CustomerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CustomerService(private val customerRepository: CustomerRepository) {
    fun getAllCustomers() = customerRepository.findAll()
    fun getCustomerById(id: Long) = customerRepository.findByIdOrNull(id) ?: throw RuntimeException("Customer not found")
    fun createCustomer(customer: CustomerEntity) = customerRepository.save(customer)
    fun updateCustomer(id: Long, customer: CustomerEntity): CustomerEntity {
        val existing = getCustomerById(id)
        return customerRepository.save(existing.copy(name = customer.name, document = customer.document))
    }
    fun deleteCustomer(id: Long) = customerRepository.deleteById(id)
}
