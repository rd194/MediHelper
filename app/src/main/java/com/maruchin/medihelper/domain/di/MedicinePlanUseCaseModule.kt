package com.maruchin.medihelper.domain.di

import com.maruchin.medihelper.domain.usecases.plans.*
import com.maruchin.medihelper.domain.usecasesimpl.plans.*
import org.koin.dsl.module

val medicinePlanUseCaseModule = module {
    factory { 
        GetLivePlansItemsByProfileUseCaseImpl(
            planRepo = get(),
            medicineRepo = get()
        ) as GetLivePlansItemsByProfileUseCase
    }
    factory {
        GetPlanDetailsUseCaseImpl(
            planRepo = get(),
            medicineRepo = get(),
            profileRepo = get()
        ) as GetPlanDetailsUseCase
    }
    factory {
        GetPlanEditDataUseCaseImpl(
            planRepo = get()
        ) as GetPlanEditDataUseCase
    }
    factory { 
        SavePlanUseCaseImpl(
            planRepo = get(),
            plannedMedicineRepo = get(),
            plannedMedicineScheduler = get(),
            deviceCalendar = get(),
            validator = get()
        ) as SavePlanUseCase
    }
    factory {
        DeleteSinglePlanUseCaseImpl(
            planRepo = get(),
            plannedMedicineRepo = get(),
            deletePlannedMedicinesUseCase = get()
        ) as DeleteSinglePlanUseCase
    }
    factory {
        DeletePlansUseCaseImpl(
            planRepo = get(),
            plannedMedicineRepo = get(),
            deletePlannedMedicinesUseCase = get()
        ) as DeletePlansUseCase
    }
    factory { 
        GetPlanHistoryUseCaseImpl(
            plannedMedicineRepo = get()
        ) as GetPlanHistoryUseCase
    }
}