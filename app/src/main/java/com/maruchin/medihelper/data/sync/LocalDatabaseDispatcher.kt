package com.maruchin.medihelper.data.sync

import com.maruchin.medihelper.data.local.DeletedHistory
import com.maruchin.medihelper.data.local.dao.MedicineDao
import com.maruchin.medihelper.data.local.dao.MedicinePlanDao
import com.maruchin.medihelper.data.local.dao.PersonDao
import com.maruchin.medihelper.data.local.dao.PlannedMedicineDao
import com.maruchin.medihelper.data.local.model.MedicineEntity
import com.maruchin.medihelper.data.local.model.MedicinePlanEntity
import com.maruchin.medihelper.data.local.model.PersonEntity
import com.maruchin.medihelper.data.local.model.PlannedMedicineEntity
import com.maruchin.medihelper.data.remote.dto.MedicineDto
import com.maruchin.medihelper.data.remote.dto.MedicinePlanDto
import com.maruchin.medihelper.data.remote.dto.PersonDto
import com.maruchin.medihelper.data.remote.dto.PlannedMedicineDto

class LocalDatabaseDispatcher(
    private val entityDtoMapper: EntityDtoMapper,
    private val medicineDao: MedicineDao,
    private val personDao: PersonDao,
    private val medicinePlanDao: MedicinePlanDao,
    private val plannedMedicineDao: PlannedMedicineDao,
    private val deletedHistory: DeletedHistory
) {
    suspend fun dispatchMedicinesChanges(medicineDtoList: List<MedicineDto>) {
        val remoteIdList = medicineDtoList.map { it.medicineRemoteId!! }
        val updateList = mutableListOf<MedicineEntity>()
        val insertList = mutableListOf<MedicineEntity>()
        medicineDtoList.forEach { medicineDto ->
            val medicineEntity = entityDtoMapper.medicineDtoToEntity(medicineDto)
            if (medicineEntity.medicineId != 0) {
                updateList.add(medicineEntity)
            } else {
                val existingMedicineID =
                    medicineDao.getIdByRemoteId(medicineEntity.medicineRemoteId!!)
                if (existingMedicineID != null) {
                    updateList.add(medicineEntity.copy(medicineId = existingMedicineID))
                } else {
                    insertList.add(medicineEntity)
                }
            }
        }
        with(medicineDao) {
            deleteByRemoteIdNotIn(remoteIdList)
            update(updateList)
            insert(insertList)
        }
        deletedHistory.clearMedicineHistory()
    }

    suspend fun dispatchPersonsChanges(personDtoList: List<PersonDto>) {
        val remoteIdList = personDtoList.map { it.personRemoteId!! }
        val updateList = mutableListOf<PersonEntity>()
        val insertList = mutableListOf<PersonEntity>()
        personDtoList.forEach { personDto ->
            val personEntity = entityDtoMapper.personDtoToEntity(personDto)
            if (personEntity.personId != 0) {
                updateList.add(personEntity)
            } else {
                val existingPersonID =
                    personDao.getIdByRemoteId(personEntity.personRemoteId!!)
                if (existingPersonID != null) {
                    updateList.add(personEntity.copy(personId = existingPersonID))
                } else {
                    insertList.add(personEntity)
                }
            }
        }
        with(personDao) {
            deleteByRemoteIdNotIn(remoteIdList)
            update(updateList)
            insert(insertList)
        }
        deletedHistory.clearPersonHistory()
    }

    suspend fun dispatchMedicinesPlansChanges(medicinePlanDtoList: List<MedicinePlanDto>) {
        val remoteIdList = medicinePlanDtoList.map { it.medicinePlanRemoteId!! }
        val updateList = mutableListOf<MedicinePlanEntity>()
        val insertList = mutableListOf<MedicinePlanEntity>()
        medicinePlanDtoList.forEach { medicinePlanDto ->
            val medicinePlanEntity = entityDtoMapper.medicinePlanDtoToEntity(medicinePlanDto)
            if (medicinePlanEntity.medicinePlanId != 0) {
                updateList.add(medicinePlanEntity)
            } else {
                val existingMedicinePlanID =
                    medicinePlanDao.getIdByRemoteId(medicinePlanEntity.medicinePlanRemoteId!!)
                if (existingMedicinePlanID != null) {
                    updateList.add(medicinePlanEntity.copy(medicinePlanId = existingMedicinePlanID))
                } else {
                    insertList.add(medicinePlanEntity)
                }
            }
        }
        with(medicinePlanDao) {
            deleteByRemoteIdNotIn(remoteIdList)
            update(updateList)
            insert(insertList)
        }
        deletedHistory.clearMedicinePlanHistory()
    }

    suspend fun dispatchPlannedMedicinesChanges(plannedMedicineDtoList: List<PlannedMedicineDto>) {
        val remoteIdList = plannedMedicineDtoList.map { it.plannedMedicineRemoteId!! }
        val updateList = mutableListOf<PlannedMedicineEntity>()
        val insertList = mutableListOf<PlannedMedicineEntity>()
        plannedMedicineDtoList.forEach { plannedMedicineDto ->
            val plannedMedicineEntity = entityDtoMapper.plannedMedicineDtoToEntity(plannedMedicineDto)
            if (plannedMedicineEntity.plannedMedicineId != 0) {
                updateList.add(plannedMedicineEntity)
            } else {
                val existingPlannedMedicineID =
                    plannedMedicineDao.getIdByRemoteId(plannedMedicineEntity.plannedMedicineRemoteId!!)
                if (existingPlannedMedicineID != null) {
                    updateList.add(plannedMedicineEntity.copy(plannedMedicineId = existingPlannedMedicineID))
                } else {
                    insertList.add(plannedMedicineEntity)
                }
            }
        }
        with(plannedMedicineDao) {
            deleteByRemoteIdNotIn(remoteIdList)
            update(updateList)
            insert(insertList)
        }
        deletedHistory.clearPlannedMedicineHistory()
    }
}