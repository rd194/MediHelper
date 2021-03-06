package com.maruchin.medihelper.presentation.feature.add_edit_plan

import androidx.lifecycle.*
import com.maruchin.medihelper.domain.device.DeviceCalendar
import com.maruchin.medihelper.domain.entities.*
import com.maruchin.medihelper.domain.model.PlanErrors
import com.maruchin.medihelper.domain.usecases.medicines.GetMedicineSimpleItemUseCase
import com.maruchin.medihelper.domain.usecases.plans.GetPlanEditDataUseCase
import com.maruchin.medihelper.domain.usecases.plans.SavePlanUseCase
import com.maruchin.medihelper.domain.usecases.profile.GetProfileSimpleItemUseCase
import com.maruchin.medihelper.presentation.framework.ActionLiveData
import kotlinx.coroutines.launch

class AddEditPlanViewModel(
    private val getPlanEditDataUseCase: GetPlanEditDataUseCase,
    private val getProfileSimpleItemUseCase: GetProfileSimpleItemUseCase,
    private val getMedicineSimpleItemUseCase: GetMedicineSimpleItemUseCase,
    private val savePlanUseCase: SavePlanUseCase,
    private val deviceCalendar: DeviceCalendar
) : ViewModel() {

    val formTitle: LiveData<String>
    val colorPrimary: LiveData<String>
    val planType = MutableLiveData<Plan.Type>()
    val startDate = MutableLiveData<AppDate>()
    val endDate = MutableLiveData<AppDate>()
    val intakeDaysType = MutableLiveData<IntakeDaysType>()
    val daysOfWeek = MutableLiveData<IntakeDays.DaysOfWeek>()

    val medicine: LiveData<MedicineBasicData>
        get() = _medicine
    val profile: LiveData<ProfileBasicData>
        get() = _profile
    val interval: LiveData<IntakeDays.Interval>
        get() = _interval
    val sequence: LiveData<IntakeDays.Sequence>
        get() = _sequence
    val timeDoseList: LiveData<List<TimeDose>>
        get() = _timeDoseList
    val loadingInProgress: LiveData<Boolean>
        get() = _loadingInProgress
    val actionDataLoaded: LiveData<Boolean>
        get() = _actionDataLoaded
    val actionMedicinePlanSaved: LiveData<Boolean>
        get() = _actionMedicinePlanSaved
    val errorEndDate: LiveData<String>
        get() = _errorEndDate
    val errorGlobal: LiveData<String>
        get() = _errorGlobal

    private val _medicine = MutableLiveData<MedicineBasicData>()
    private val _profile = MutableLiveData<ProfileBasicData>()
    private val _interval = MutableLiveData<IntakeDays.Interval>()
    private val _sequence = MutableLiveData<IntakeDays.Sequence>()
    private val _timeDoseList = MutableLiveData<List<TimeDose>>()
    private val _loadingInProgress = MutableLiveData<Boolean>(false)
    private val _actionDataLoaded = ActionLiveData()
    private val _actionMedicinePlanSaved = ActionLiveData()
    private val _errorEndDate = MutableLiveData<String>()
    private val _errorGlobal = MutableLiveData<String>()

    private val editPlanId = MutableLiveData<String>(null)

    init {
        formTitle = getLiveFormTitle()
        colorPrimary = getLiveColorPrimary()
    }

    fun initViewModel(medicinePlanId: String?, profileId: String?, medicineId: String?) = viewModelScope.launch {
        if (medicinePlanId != null) {
            editPlanId.postValue(medicinePlanId)
            loadAndSetEditData(medicinePlanId)
        } else if (profileId != null && medicineId != null) {
            loadAndSetProfileData(profileId)
            loadAndSetMedicineData(medicineId)
            setDefaultData()
        } else {
            throw Exception("Invalid init parameters")
        }
        _actionDataLoaded.sendAction()
    }

    fun setInterval(value: Int) {
        _interval.value = IntakeDays.Interval(daysCount = value)
    }

    fun setIntakeDays(value: Int) {
        val currSequence = _sequence.value
        _sequence.value = currSequence?.copy(intakeCount = value)
    }

    fun setNoIntakeDays(value: Int) {
        val currSequence = _sequence.value
        _sequence.value = currSequence?.copy(notIntakeCount = value)
    }

    fun addTimeDose() {
        _timeDoseList.value?.let { list ->
            val mutableList = list.toMutableList()
            mutableList.add(
                TimeDose(
                    time = AppTime(8, 0),
                    doseSize = 1f
                )
            )
            _timeDoseList.value = mutableList.toList()
        }
    }

    fun updateTimeDose(timeDoseEditData: TimeDoseEditData) {
        _timeDoseList.value?.let { list ->
            val mutableList = list.toMutableList()
            mutableList[timeDoseEditData.position] = timeDoseEditData.toDomainModel()
            _timeDoseList.value = mutableList.toList()
        }
    }

    fun deleteTimeDose(position: Int) {
        _timeDoseList.value?.let { list ->
            val mutableList = list.toMutableList()
            mutableList.removeAt(position)
            _timeDoseList.value = mutableList.toList()
        }
    }

    fun saveMedicinePlan() = viewModelScope.launch {
        _loadingInProgress.postValue(true)
        val params = getSaveParams()
        val errors = savePlanUseCase.execute(params)
        _loadingInProgress.postValue(false)

        if (errors.noErrors) {
            _actionMedicinePlanSaved.sendAction()
        } else {
            postErrors(errors)
        }
    }

    private fun getSaveParams() = SavePlanUseCase.Params(
        medicinePlanId = editPlanId.value,
        profileId = profile.value?.profileId,
        medicineId = medicine.value?.medicineId,
        planType = planType.value,
        startDate = startDate.value,
        endDate = endDate.value,
        intakeDays = getIntakeDays(),
        timeDoseList = timeDoseList.value
    )

    private fun getIntakeDays(): IntakeDays? {
        return when (intakeDaysType.value) {
            IntakeDaysType.EVERYDAY -> IntakeDays.Everyday
            IntakeDaysType.DAYS_OF_WEEK -> daysOfWeek.value
            IntakeDaysType.INTERVAL -> interval.value
            IntakeDaysType.SEQUENCE -> sequence.value
            else -> null
        }
    }

    private fun getLiveFormTitle() = Transformations.map(editPlanId) { planId ->
        if (planId == null) {
            "Zaplanuj lek"
        } else {
            "Edytuj plan"
        }
    }

    private fun getLiveColorPrimary() = Transformations.map(_profile) { profile ->
        profile.color
    }

    private suspend fun loadAndSetEditData(medicinePlanId: String) {
        val editData = getPlanEditDataUseCase.execute(medicinePlanId)
        loadAndSetProfileData(editData.profileId)
        loadAndSetMedicineData(editData.medicineId)
        planType.postValue(editData.planType)
        startDate.postValue(editData.startDate)
        endDate.postValue(editData.endDate)
        setIntakeDays(editData.intakeDays)
        _timeDoseList.postValue(editData.timeDoseList)
    }

    private suspend fun loadAndSetProfileData(profileId: String) {
        val simpleItem = getProfileSimpleItemUseCase.execute(profileId)
        val data = ProfileBasicData.fromDomainModel(simpleItem)
        _profile.postValue(data)

    }

    private suspend fun loadAndSetMedicineData(medicineId: String) {
        val simpleItem = getMedicineSimpleItemUseCase.execute(medicineId)
        val data = MedicineBasicData.fromDomainModel(simpleItem)
        _medicine.postValue(data)
    }

    private fun setIntakeDays(intakeDays: IntakeDays?) {
        when (intakeDays) {
            null -> setIntakeDaysNull()
            is IntakeDays.Everyday -> setIntakeDaysEveryday()
            is IntakeDays.DaysOfWeek -> setIntakeDaysDaysOfWeek(intakeDays)
            is IntakeDays.Interval -> setIntakeDaysInterval(intakeDays)
            is IntakeDays.Sequence -> setIntakeDaysSequence(intakeDays)
        }
    }

    private fun setIntakeDaysNull() {
        intakeDaysType.postValue(null)
    }

    private fun setIntakeDaysEveryday() {
        intakeDaysType.postValue(IntakeDaysType.EVERYDAY)
    }

    private fun setIntakeDaysDaysOfWeek(intakeDays: IntakeDays.DaysOfWeek) {
        intakeDaysType.postValue(IntakeDaysType.DAYS_OF_WEEK)
        daysOfWeek.postValue(intakeDays)
    }

    private fun setIntakeDaysInterval(intakeDays: IntakeDays.Interval) {
        intakeDaysType.postValue(IntakeDaysType.INTERVAL)
        _interval.postValue(intakeDays)
    }

    private fun setIntakeDaysSequence(intakeDays: IntakeDays.Sequence) {
        intakeDaysType.postValue(IntakeDaysType.SEQUENCE)
        _sequence.postValue(intakeDays)
    }

    private fun setDefaultData() {
        planType.postValue(Plan.Type.ONE_DAY)
        val currDate = deviceCalendar.getCurrDate()
        startDate.postValue(currDate)
        endDate.postValue(currDate.copy().apply { addDays(1) })
        intakeDaysType.postValue(IntakeDaysType.EVERYDAY)
        daysOfWeek.postValue(
            IntakeDays.DaysOfWeek(
                monday = true,
                tuesday = false,
                wednesday = false,
                thursday = false,
                friday = false,
                saturday = false,
                sunday = false
            )
        )
        _interval.postValue(IntakeDays.Interval(daysCount = 2))
        _sequence.postValue(IntakeDays.Sequence(intakeCount = 21, notIntakeCount = 7))
        _timeDoseList.postValue(
            listOf(
                TimeDose(
                    time = AppTime(8, 0),
                    doseSize = 1f
                )
            )
        )
    }

    private fun postErrors(errors: PlanErrors) {
        val endDateError = if (errors.emptyEndDate) {
            "Nie podano daty końca"
        } else if (errors.incorrectDatesOrder) {
            "Zła kolejność dat"
        } else null
        val globalError = if (errors.emptyPlanType) {
            "Nie wybrano czasu trwania planu"
        } else if (errors.emptyIntakeDays) {
            "Nie wybrano dni przyjmowania"
        } else null

        _errorEndDate.postValue(endDateError)
        _errorGlobal.postValue(globalError)
    }
}