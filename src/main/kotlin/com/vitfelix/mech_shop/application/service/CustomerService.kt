package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.domain.model.CustomerEntity
import com.vitfelix.mech_shop.domain.repository.CustomerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    // Remove tudo que não for número (pontos, traços, espaços, barras)
    private fun sanitizeDocument(document: String): String {
        return document.replace(Regex("[^0-9]"), "")
    }

    fun getAllCustomers() = customerRepository.findAll()

    fun getCustomerById(id: Long) = customerRepository.findByIdOrNull(id)
        ?: throw RuntimeException("Customer not found")

    fun findByDocument(document: String): CustomerEntity? {
        val cleanDocument = sanitizeDocument(document)
        return customerRepository.findByDocument(cleanDocument)
    }

    fun createCustomer(customer: CustomerEntity): CustomerEntity {
        // Limpa o documento antes de salvar
        val sanitizedCustomer = customer.copy(document = sanitizeDocument(customer.document))
        return customerRepository.save(sanitizedCustomer)
    }

    fun updateCustomer(id: Long, customer: CustomerEntity): CustomerEntity {
        val existing = getCustomerById(id)
        // Limpa o documento antes de atualizar
        val cleanDocument = sanitizeDocument(customer.document)
        return customerRepository.save(existing.copy(name = customer.name, document = cleanDocument))
    }

    fun deleteCustomer(id: Long) = customerRepository.deleteById(id)
}
