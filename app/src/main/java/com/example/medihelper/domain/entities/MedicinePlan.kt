package com.example.medihelper.domain.entities

data class MedicinePlan(
    val medicinePlanId: Int,
    val medicineId: Int,
    val personId: Int,
    val durationType: DurationType,
    val startDate: AppDate,
    val endDate: AppDate?,
    val daysType: DaysType?,
    val daysOfWeek: DaysOfWeek?,
    val intervalOfDays: Int?,
    val timeDoseList: List<TimeDose>
)