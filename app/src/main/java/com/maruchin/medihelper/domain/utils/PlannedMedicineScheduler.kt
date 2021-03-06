package com.maruchin.medihelper.domain.utils

import com.maruchin.medihelper.domain.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlannedMedicineScheduler {

    suspend fun getPlannedMedicines(plan: Plan): List<PlannedMedicine> =
        withContext(Dispatchers.Default) {
            return@withContext when (plan.planType) {
                Plan.Type.ONE_DAY -> getForDate(plan, plan.startDate)
                Plan.Type.PERIOD -> when (plan.intakeDays) {
                    is IntakeDays.Everyday -> getForEveryday(plan)
                    is IntakeDays.DaysOfWeek -> getForDaysOfWeek(plan)
                    is IntakeDays.Interval -> getForInterval(plan)
                    is IntakeDays.Sequence -> getForSequence(plan)
                    else -> emptyList()
                }
                Plan.Type.CONTINUOUS -> when (plan.intakeDays) {
                    is IntakeDays.Everyday -> getForEveryday(plan)
                    is IntakeDays.DaysOfWeek -> getForDaysOfWeek(plan)
                    is IntakeDays.Interval -> getForInterval(plan)
                    is IntakeDays.Sequence -> getForSequence(plan)
                    else -> emptyList()
                }
            }
        }

    private fun getForEveryday(plan: Plan): List<PlannedMedicine> {
        val entriesList = mutableListOf<PlannedMedicine>()
        val currDate = plan.startDate.copy()

        while (currDate <= plan.endDate!!) {
            entriesList.addAll(
                getForDate(plan, currDate.copy())
            )
            currDate.addDays(1)
        }
        return entriesList
    }

    private fun getForDaysOfWeek(plan: Plan): List<PlannedMedicine> {
        val entriesList = mutableListOf<PlannedMedicine>()
        val currDate = plan.startDate.copy()

        while (currDate <= plan.endDate!!) {
            val daysOfWeek = plan.intakeDays as IntakeDays.DaysOfWeek
            if (daysOfWeek.isDaySelected(currDate.dayOfWeek)) {
                entriesList.addAll(
                    getForDate(plan, currDate.copy())
                )
            }
            currDate.addDays(1)
        }
        return entriesList
    }

    private fun getForInterval(plan: Plan): List<PlannedMedicine> {
        val entriesList = mutableListOf<PlannedMedicine>()
        val currDate = plan.startDate.copy()

        while (currDate <= plan.endDate!!) {
            entriesList.addAll(
                getForDate(plan, currDate.copy())
            )
            val interval = plan.intakeDays as IntakeDays.Interval
            currDate.addDays(interval.daysCount)
        }
        return entriesList
    }

    private fun getForSequence(plan: Plan): List<PlannedMedicine> {
        val entriesList = mutableListOf<PlannedMedicine>()
        val currDate = plan.startDate.copy()

        val intakeDays = plan.intakeDays as IntakeDays.Sequence
        var intakeCountIterator = 1


        while (currDate <= plan.endDate!!) {
            if (intakeCountIterator > intakeDays.intakeCount) {
                currDate.addDays(intakeDays.notIntakeCount)
                intakeCountIterator = 1
            } else {
                entriesList.addAll(
                    getForDate(plan, currDate.copy())
                )
                currDate.addDays(1)
                intakeCountIterator++
            }
        }
        return entriesList
    }

    private fun getForDate(
        plan: Plan,
        plannedDate: AppDate
    ): List<PlannedMedicine> {
        val entriesList = mutableListOf<PlannedMedicine>()
        plan.timeDoseList.forEach { timeDose ->
            entriesList.add(
                PlannedMedicine(
                    entityId = "",
                    medicinePlanId = plan.entityId,
                    profileId = plan.profileId,
                    medicineId = plan.medicineId,
                    plannedDate = plannedDate,
                    plannedTime = timeDose.time,
                    plannedDoseSize = timeDose.doseSize,
                    status = PlannedMedicine.Status.NOT_TAKEN
                )
            )
        }
        return entriesList
    }
}