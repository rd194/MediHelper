package com.maruchin.medihelper.presentation.feature.mediplan

import androidx.lifecycle.*
import com.maruchin.medihelper.domain.entities.*
import com.maruchin.medihelper.domain.model.MedicineItem
import com.maruchin.medihelper.domain.model.ProfileItem
import com.maruchin.medihelper.domain.usecases.datetime.GetCurrDateUseCase
import com.maruchin.medihelper.domain.usecases.medicines.GetMedicineItemUseCase
import com.maruchin.medihelper.domain.usecases.mediplans.SaveMedicinePlanUseCase
import com.maruchin.medihelper.domain.usecases.profile.GetProfileItemUseCase
import com.maruchin.medihelper.presentation.framework.ActionLiveData
import kotlinx.coroutines.launch

class AddEditMedicinePlanViewModel(
    private val getProfileItemUseCase: GetProfileItemUseCase,
    private val getMedicineItemUseCase: GetMedicineItemUseCase,
    private val getCurrDateUseCase: GetCurrDateUseCase,
    private val saveMedicinePlanUseCase: SaveMedicinePlanUseCase
) : ViewModel() {

    val colorPrimary: LiveData<String>
    val profileName: LiveData<String>
    val medicineName: LiveData<String>
    val planType = MutableLiveData<MedicinePlan.Type>()
    val startDate = MutableLiveData<AppDate>()
    val endDate = MutableLiveData<AppDate>()
    val intakeDaysType = MutableLiveData<IntakeDaysType>()
    val daysOfWeek = MutableLiveData<IntakeDays.DaysOfWeek>()

    val medicineUnit: String?
        get() = medicineItem.value?.unit
    val formTitle: LiveData<String>
        get() = _formTitle
    val interval: LiveData<IntakeDays.Interval>
        get() = _interval
    val sequence: LiveData<IntakeDays.Sequence>
        get() = _sequence
    val timeDoseList: LiveData<List<TimeDose>>
        get() = _timeDoseList
    val actionDataLoaded: LiveData<Boolean>
        get() = _actionDataLoaded
    val loadingInProgress: LiveData<Boolean>
        get() = _loadingInProgress
    val actionMedicinePlanSaved: LiveData<Boolean>
        get() = _actionMedicinePlanSaved

    private val _formTitle = MutableLiveData<String>("Zaplanuj lek")
    private val _interval = MutableLiveData<IntakeDays.Interval>()
    private val _sequence = MutableLiveData<IntakeDays.Sequence>()
    private val _timeDoseList = MutableLiveData<List<TimeDose>>()
    private val _actionDataLoaded = ActionLiveData()
    private val _loadingInProgress = MutableLiveData<Boolean>(false)
    private val _actionMedicinePlanSaved = ActionLiveData()

    private val profileItem = MutableLiveData<ProfileItem>()
    private val medicineItem = MutableLiveData<MedicineItem>()

    init {
        colorPrimary = Transformations.map(profileItem) { it.color }
        profileName = Transformations.map(profileItem) { it.name }
        medicineName = Transformations.map(medicineItem) { it.name }
        loadDefaultData()
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

    fun updateTimeDose(timeDoseFormItem: TimeDoseFormItem) {
        _timeDoseList.value?.let { list ->
            val mutableList = list.toMutableList()
            mutableList[timeDoseFormItem.position] = timeDoseFormItem.toTimeDose()
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
        val params = SaveMedicinePlanUseCase.Params(
            medicinePlanId = null,
            profileId = profileItem.value?.profileId,
            medicineId = medicineItem.value?.medicineId,
            planType = planType.value,
            startDate = startDate.value,
            endDate = endDate.value,
            intakeDays = when (intakeDaysType.value) {
                IntakeDaysType.EVERYDAY -> IntakeDays.Everyday
                IntakeDaysType.DAYS_OF_WEEK -> daysOfWeek.value
                IntakeDaysType.INTERVAL -> interval.value
                IntakeDaysType.SEQUENCE -> sequence.value
                else -> null
            },
            timeDoseList = timeDoseList.value
        )
        val validator = saveMedicinePlanUseCase.execute(params)
        _loadingInProgress.postValue(false)

        if (validator.noErrors) {
            _actionMedicinePlanSaved.sendAction()
        } else {
            //todo postErrors
        }
    }

    fun setArgs(args: AddEditMedicinePlanFragmentArgs) = viewModelScope.launch {
        val profileItemValue = getProfileItemUseCase.execute(args.profileId)
        val medicineItemValue = getMedicineItemUseCase.execute(args.medicineId)

        profileItem.postValue(profileItemValue)
        medicineItem.postValue(medicineItemValue)
        _actionDataLoaded.sendAction()
    }

    private fun loadDefaultData() {
        planType.postValue(MedicinePlan.Type.ONCE)
        val currDate = getCurrDateUseCase.execute()
        startDate.postValue(currDate)
        endDate.postValue(currDate.apply { addDays(1) })
        intakeDaysType.postValue(IntakeDaysType.EVERYDAY)
        daysOfWeek.postValue(
            IntakeDays.DaysOfWeek(
                monday = false,
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

    enum class IntakeDaysType {
        EVERYDAY, DAYS_OF_WEEK, INTERVAL, SEQUENCE
    }

    data class TimeDoseFormItem(
        val position: Int,
        val time: AppTime,
        val doseSize: Float
    ) {
        constructor(timeDose: TimeDose, position: Int) : this(
            position = position,
            time = timeDose.time,
            doseSize = timeDose.doseSize
        )

        fun toTimeDose() = TimeDose(time, doseSize)
    }
}