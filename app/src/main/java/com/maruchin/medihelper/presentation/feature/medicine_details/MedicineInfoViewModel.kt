package com.maruchin.medihelper.presentation.feature.medicine_details

import androidx.lifecycle.*
import com.maruchin.medihelper.domain.entities.MedicineInfo
import com.maruchin.medihelper.domain.entities.MedicineInfoSearchResult
import com.maruchin.medihelper.domain.usecases.medicines.GetMedicineInfoUseCase
import com.maruchin.medihelper.domain.usecases.medicines.SearchForMedicineInfoUseCase
import kotlinx.coroutines.launch

class MedicineInfoViewModel(
    private val searchForMedicineInfoUseCase: SearchForMedicineInfoUseCase,
    private val getMedicineInfoUseCase: GetMedicineInfoUseCase
) : ViewModel() {

    val noResults: LiveData<Boolean>
    //todo tutaj wykorzystywane są bezpośrednio encje, pomyśleć czy nie da się lepiej
    val searchResults: LiveData<List<MedicineInfoSearchResult>>
        get() = _searchResults
    val medicineInfo: LiveData<List<MedicineInfo>>
        get() = _medicineInfo
    val loadingInProgress: LiveData<Boolean>
        get() = _loadingInProgress

    private val _searchResults = MutableLiveData<List<MedicineInfoSearchResult>>()
    private val _medicineInfo = MutableLiveData<List<MedicineInfo>>()
    private val _loadingInProgress = MutableLiveData<Boolean>(false)

    init {
        noResults = Transformations.map(searchResults) { it.isEmpty() }
    }

    fun setArgs(args: MedicineInfoDialogArgs) = viewModelScope.launch {
        _loadingInProgress.postValue(true)
        val searchResults = searchForMedicineInfoUseCase.execute(args.medicineName)
        _searchResults.postValue(searchResults)
        _loadingInProgress.postValue(false)
    }

    fun getMedicineInfo(urlString: String) = viewModelScope.launch {
        _loadingInProgress.postValue(true)
        val info = getMedicineInfoUseCase.execute(urlString)
        _medicineInfo.postValue(info)
        _loadingInProgress.postValue(false)
    }
}