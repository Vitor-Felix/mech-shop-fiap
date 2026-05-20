package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.CreateWorkOrderDto
import com.vitfelix.mech_shop.application.dto.WorkOrderResponse
import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import com.vitfelix.mech_shop.domain.model.*
import com.vitfelix.mech_shop.domain.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class WorkOrderService(
    private val customerRepository: CustomerRepository,
    private val vehicleRepository: VehicleRepository,
    private val workRepository: WorkRepository,
    private val partRepository: PartRepository,
    private val workOrderRepository: WorkOrderRepository
) {

    @Transactional
    fun createWorkOrder(dto: CreateWorkOrderDto): WorkOrderEntity {
        // Validação de dados sensíveis
        validateSensitiveData(dto.customerDocument, dto.vehicleLicensePlate)

        val customer = customerRepository.findByDocument(dto.customerDocument)
            ?: customerRepository.save(CustomerEntity(document = dto.customerDocument, name = dto.customerName))

        // 2. Identificação/Cadastro do Veículo
        val vehicle = vehicleRepository.findByLicensePlate(dto.vehicleLicensePlate)
            ?: vehicleRepository.save(
                VehicleEntity(
                    licensePlate = dto.vehicleLicensePlate.uppercase(),
                    brand = dto.vehicleBrand,
                    model = dto.vehicleModel,
                    year = dto.vehicleYear,
                    customer = customer
                )
            )

        // 3. Busca de Serviços e Peças solicitados
        val works = workRepository.findAllById(dto.serviceIds)
        val parts = partRepository.findAllById(dto.partIds)

        // 4. Instanciação da OS com status inicial RECEBIDA
        val workOrder = WorkOrderEntity(
            customer = customer,
            vehicle = vehicle,
            status = WorkOrderStatusEnum.RECEIVED,
            works = works.toMutableList(),
            parts = parts.toMutableList()
        )

        // 5. Orçamento gerado automaticamente (Regra de Domínio)
        workOrder.calculateBudget()

        // 6. Envio fictício do orçamento (Simulando o requisito do MVP alterando o status)
        workOrder.status = WorkOrderStatusEnum.WAITING_APPROVAL
        return workOrderRepository.save(workOrder)
    }

    fun getWorkOrderById(id: Long): WorkOrderEntity {
        return workOrderRepository.findById(id)
            .orElseThrow { RuntimeException("Work Order not found with id: $id") }
    }

    @Transactional
    fun updateStatus(id: Long, newStatus: WorkOrderStatusEnum): WorkOrderEntity {
        val workOrder = getWorkOrderById(id)

        // Alteração automática dos timestamps baseada no status
        if (newStatus == WorkOrderStatusEnum.IN_EXECUTION) {
            workOrder.startedAt = LocalDateTime.now()
        } else if (newStatus == WorkOrderStatusEnum.FINISHED) {
            workOrder.finishedAt = LocalDateTime.now()

        }

        workOrder.status = newStatus
        return workOrderRepository.save(workOrder)
    }

    // Monitoramento do tempo médio de execução (em minutos)
    fun getAverageExecutionTimeInMinutes(): Double {
        val completedOrders = workOrderRepository.findAll().filter {
            it.startedAt != null && it.finishedAt != null
        }
        if (completedOrders.isEmpty()) return 0.0

        val totalMinutes = completedOrders.sumOf {
            Duration.between(it.startedAt, it.finishedAt).toSeconds()
        }
        return totalMinutes.toDouble() / completedOrders.size
    }

    fun getAllWorkOrders(): List<WorkOrderResponse> {
        return workOrderRepository.findAll().map {
            WorkOrderResponse(
                it.id,
                it.customer.name,
                it.vehicle.licensePlate,
                it.status,
                it.totalAmount,
                it.startedAt,
                it.finishedAt
            )
        }
    }

    private fun validateSensitiveData(document: String, licensePlate: String) {
        val documentRegex = "^(\\d{11}|\\d{14})$".toRegex() // 11 dígitos (CPF) ou 14 (CNPJ)
        val plateRegex = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$".toRegex() // Padrão Nacional e Mercosul

        if (!documentRegex.matches(document)) {
            throw IllegalArgumentException("Invalid CPF/CNPJ format. Must contain only 11 or 14 digits.")
        }
        if (!plateRegex.matches(licensePlate.uppercase())) {
            throw IllegalArgumentException("Invalid License Plate format.")
        }
    }
}
