package com.maruchin.medihelper.presentation.feature.add_edit_plan

import com.maruchin.medihelper.domain.model.MedicineSimpleItem

data class MedicineBasicData(
    val medicineId: String,
    val name: String,
    val unit: String
) {
    companion object {

        fun fromDomainModel(model: MedicineSimpleItem) =
            MedicineBasicData(
                medicineId = model.medicineId,
                name = model.name,
                unit = model.unit
            )
    }
}