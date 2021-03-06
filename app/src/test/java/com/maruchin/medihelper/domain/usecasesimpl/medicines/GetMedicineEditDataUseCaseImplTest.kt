package com.maruchin.medihelper.domain.usecasesimpl.medicines

import com.google.common.truth.Truth
import com.maruchin.medihelper.domain.entities.AppExpireDate
import com.maruchin.medihelper.domain.entities.Medicine
import com.maruchin.medihelper.domain.entities.MedicineState
import com.maruchin.medihelper.domain.model.MedicineEditData
import com.maruchin.medihelper.domain.repositories.MedicineRepo
import com.maruchin.medihelper.domain.usecases.medicines.GetMedicineEditDataUseCase
import com.maruchin.medihelper.testingframework.mock
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class GetMedicineEditDataUseCaseImplTest {

    private val medicineRepo: MedicineRepo = mock()

    private lateinit var useCase: GetMedicineEditDataUseCase

    @Before
    fun before() {
        useCase =
            GetMedicineEditDataUseCaseImpl(
                medicineRepo
            )
    }

    @Test
    fun execute() {
        val medicineId = "abc"
        val medicine = Medicine(
            entityId = medicineId,
            name = "Hitaxa",
            unit = "tabletki",
            expireDate = AppExpireDate(2020, 3),
            type = "Na katar",
            state = MedicineState(
                packageSize = 100f,
                currState = 70f
            ),
            pictureName = "test.jpg"
        )

        val expectedResult = MedicineEditData(
            medicineId = medicineId,
            name = "Hitaxa",
            unit = "tabletki",
            expireDate = AppExpireDate(2020, 3),
            type = "Na katar",
            state = MedicineState(
                packageSize = 100f,
                currState = 70f
            ),
            pictureName = "test.jpg"
        )

        runBlocking {
            Mockito.`when`(medicineRepo.getById(medicineId)).thenReturn(medicine)

            val result = useCase.execute(medicineId)

            Truth.assertThat(result).isEqualTo(expectedResult)
        }
    }
}