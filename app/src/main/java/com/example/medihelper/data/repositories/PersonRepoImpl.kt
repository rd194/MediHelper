package com.example.medihelper.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.medihelper.data.local.SharedPref
import com.example.medihelper.data.local.dao.PersonDao
import com.example.medihelper.data.local.model.PersonEntity
import com.example.medihelper.domain.entities.Person
import com.example.medihelper.domain.repositories.PersonRepo

class PersonRepoImpl(
    private val personDao: PersonDao,
    private val sharedPref: SharedPref
) : PersonRepo {


    override suspend fun insert(person: Person) {
        val newEntity = PersonEntity(person = person, personRemoteId = null)
        personDao.insert(newEntity)
    }

    override suspend fun update(person: Person) {
        val existingPersonRemoteId = personDao.getRemoteIdById(person.personId)
        val updatedEntity = PersonEntity(person = person, personRemoteId = existingPersonRemoteId)
        personDao.update(updatedEntity)
    }

    override suspend fun deleteById(id: Int) {
        personDao.deleteById(id)
    }

    override suspend fun getById(id: Int): Person {
        val personEntity = personDao.getById(id)
        return personEntity.toPerson()
    }

    override suspend fun getMainId(): Int? {
        return personDao.getMainId()
    }

    override fun getLiveById(id: Int): LiveData<Person> {
        val personEntityLive = personDao.getLiveById(id)
        return Transformations.map(personEntityLive) { it.toPerson() }
    }

    override fun getMainLive(): LiveData<Person> {
        val mainPersonEntityLive = personDao.getMainLive()
        return Transformations.map(mainPersonEntityLive) { it.toPerson() }
    }

    override fun getMainIdLive(): LiveData<Int> {
        return personDao.getMainIdLive()
    }

    override fun getMainPersonColorIdLive(): LiveData<Int> {
        return personDao.getMainColorIdLive()
    }

    override fun getAllListLive(): LiveData<List<Person>> {
        val entityList = personDao.getAllListLive()
        return Transformations.map(entityList) { list ->
            list.map { it.toPerson() }
        }
    }

    override fun getListLiveByMedicineId(id: Int): LiveData<List<Person>> {
        val entityList = personDao.getListLiveByMedicineId(id)
        return Transformations.map(entityList) { list ->
            list.map { it.toPerson() }
        }
    }

    override fun getColorIdList(): List<Int> {
        return sharedPref.getPersonColorIdList()
    }

}