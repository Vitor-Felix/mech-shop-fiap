package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.CreateWorkOrderDto
import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import com.vitfelix.mech_shop.domain.model.*
import com.vitfelix.mech_shop.domain.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
        // 1. Identificação/Cadastro do Cliente
        val customer = customerRepository.findByDocument(dto.customerDocument)
            ?: customerRepository.save(CustomerEntity(document = dto.customerDocument, name = dto.customerName))

        // 2. Identificação/Cadastro do Veículo
        val vehicle = vehicleRepository.findByLicensePlate(dto.vehicleLicensePlate)
            ?: vehicleRepository.save(
                VehicleEntity(
                    licensePlate = dto.vehicleLicensePlate,
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
}
