package com.example.medihelper.mainapp.addmedicine

import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.medihelper.AppRepository
import com.example.medihelper.localdatabase.entities.MedicineEntity
import com.example.medihelper.localdatabase.pojos.MedicineEditData
import java.io.File
import java.util.*

class AddMedicineViewModel : ViewModel() {
    private val TAG = AddMedicineViewModel::class.simpleName

    val medicineUnitListLive = AppRepository.getMedicineUnitListLive()

    val medicineNameLive = MutableLiveData<String>()
    val medicineUnitLive = MutableLiveData<String>()
    val packageSizeLive = MutableLiveData<String>()
    val currStateLive = MutableLiveData<String>()
    val expireDateLive = MutableLiveData<Date>()
    val commentsLive = MutableLiveData<String>()
    val photoFileLive = MutableLiveData<File>()

    private val selectedMedicineIDLive = MutableLiveData<Int>()
    private val selectedMedicineEditDataLive: LiveData<MedicineEditData>
    private val selectedMedicineEditDataObserver: Observer<MedicineEditData>

    init {
        selectedMedicineEditDataLive = Transformations.switchMap(selectedMedicineIDLive) { medicineId ->
            AppRepository.getMedicineEditDataLive(medicineId)
        }
        selectedMedicineEditDataObserver = Observer { medicineEditData ->
            medicineEditData?.run {
                medicineNameLive.value = medicineName
                medicineUnitLive.value = medicineUnit
                packageSizeLive.value = packageSize.toString()
                currStateLive.value = currState.toString()
                expireDateLive.value = expireDate
                commentsLive.value = comments
                photoFileLive.value = photoFilePath?.let { photoFilePath ->
                    File(photoFilePath)
                }
            }
        }
        selectedMedicineEditDataLive.observeForever(selectedMedicineEditDataObserver)
    }

    fun setSelectedMedicineID(medicineID: Int) {
        selectedMedicineIDLive.value = medicineID
    }

    override fun onCleared() {
        super.onCleared()
        selectedMedicineEditDataLive.removeObserver(selectedMedicineEditDataObserver)
    }

    fun setMedicineType(position: Int) {
        medicineUnitListLive.value?.let { medicineUnitList ->
            medicineUnitLive.value = medicineUnitList[position]
        }
    }

    fun saveMedicine(): Boolean {
        Log.d(TAG, "onClickSaveNewMedicine")
        val medicineName = medicineNameLive.value
        val medicineUnit = medicineUnitLive.value
        val packageSize = packageSizeLive.value
        val currState = currStateLive.value
        val photoFilePath = photoFileLive.value?.let { photoFile ->
            AppRepository.createPhotoFileFromTemp(photoFile).absolutePath
        }
        val expireDate = expireDateLive.value
        val comments = commentsLive.value

        if (medicineName == null || medicineUnit == null) {
            return false
        }

        val medicineEntity = MedicineEntity(
            medicineName = medicineName,
            medicineUnit = medicineUnit,
            packageSize = packageSize?.toFloat(),
            currState = currState?.toFloat(),
            photoFilePath = photoFilePath,
            expireDate = expireDate,
            comments = comments
        )
        selectedMedicineEditDataLive.value?.let { medicineEditData ->
            val medicineEntityToUpdate = medicineEntity.copy(medicineID = medicineEditData.medicineID)
            AppRepository.updateMedicine(medicineEntityToUpdate)
            return true
        }

        AppRepository.insertMedicine(medicineEntity)
        return true
    }

    fun takePhotoIntent(activity: FragmentActivity): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                val photoFile = AppRepository.createTempPhotoFile()
                photoFileLive.value = photoFile
                val photoURI = FileProvider.getUriForFile(
                    activity,
                    "com.example.medihelper.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
        }
    }

    fun resetViewModel() {
        arrayOf(
            medicineNameLive,
            medicineUnitLive,
            packageSizeLive,
            currStateLive,
            expireDateLive,
            commentsLive,
            photoFileLive
        ).forEach { field ->
            field.value = null
        }
    }
}