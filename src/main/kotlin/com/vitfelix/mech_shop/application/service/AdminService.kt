package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.domain.model.PartEntity
import com.vitfelix.mech_shop.domain.model.WorkEntity
import com.vitfelix.mech_shop.domain.repository.PartRepository
import com.vitfelix.mech_shop.domain.repository.WorkRepository
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val workRepository: WorkRepository,
    private val partRepository: PartRepository
) {
    // CRUD - Services (Works)
    fun createWork(work: WorkEntity): WorkEntity = workRepository.save(work)
    fun getAllWorks(): List<WorkEntity> = workRepository.findAll()

    // CRUD - Parts (Peças com controle de estoque simples)
    fun createPart(part: PartEntity): PartEntity = partRepository.save(part)
    fun getAllParts(): List<PartEntity> = partRepository.findAll()
}
