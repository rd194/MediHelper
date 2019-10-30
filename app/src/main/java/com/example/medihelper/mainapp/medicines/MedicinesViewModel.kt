package com.example.medihelper.mainapp.medicines

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.medihelper.localdatabase.entity.MedicineEntity
import com.example.medihelper.localdatabase.pojos.MedicineItem
import com.example.medihelper.localdatabase.repositories.MedicineRepository
import com.example.medihelper.localdatabase.repositories.PersonRepository
import com.example.medihelper.services.SharedPrefService
import java.io.File

class MedicinesViewModel(
    private val appFilesDir: File,
    private val medicineRepository: MedicineRepository,
    private val personRepository: PersonRepository,
    private val sharedPrefService: SharedPrefService
) : ViewModel() {
    private val TAG = "MedicinesViewModel"

    val isAppModeConnectedLive: LiveData<Boolean>
    val searchQueryLive = MutableLiveData("")
    val medicineItemListLive: LiveData<List<MedicineItem>>
    val medicineAvailableLive: LiveData<Boolean>
    val colorPrimaryLive: LiveData<Int>
    private val mainPersonItemLive = personRepository.getMainPersonItemLive()

    init {
        isAppModeConnectedLive = Transformations.map(sharedPrefService.getAppModeLive()) {
            it == SharedPrefService.AppMode.CONNECTED
        }
        medicineItemListLive = Transformations.switchMap(searchQueryLive) { searchQuery ->
            Log.d(TAG, "searchQuery change = $searchQuery")
            if (searchQuery.isNullOrEmpty()) {
                medicineRepository.getItemListLive()
            } else {
                medicineRepository.getFilteredItemListLive(searchQuery)
            }
        }
        medicineAvailableLive = Transformations.map(medicineItemListLive) { list ->
            list != null && list.isNotEmpty()
        }
        colorPrimaryLive = Transformations.map(mainPersonItemLive) { it.personColorResID }
    }

    fun getMedicineItemDisplayData(medicineItem: MedicineItem): MedicineItemDisplayData {
        val medicineStateData = MedicineEntity.StateData(medicineItem.packageSize, medicineItem.currState)
        return MedicineItemDisplayData(
            medicineID = medicineItem.medicineID,
            medicineName = medicineItem.medicineName,
            medicineUnit = medicineItem.medicineUnit,
            stateAvailable = medicineStateData.stateAvailable,
            stateLayoutWeight = medicineStateData.stateWeight,
            emptyLayoutWeight = medicineStateData.emptyWeight,
            stateColorId = medicineStateData.colorId,
            medicineImageFile = medicineItem.imageName?.let { File(appFilesDir, it) }
        )
    }

    data class MedicineItemDisplayData(
        val medicineID: Int,
        val medicineName: String,
        val medicineUnit: String,
        val stateAvailable: Boolean,
        val stateLayoutWeight: Float?,
        val emptyLayoutWeight: Float?,
        val stateColorId: Int?,
        val medicineImageFile: File?
    )
}