package com.maruchin.medihelper.domain.entities

data class MedicineState(
    var packageSize: Float,
    var currState: Float
) {
    val type: Type
        get() {
            val statePercent = currState / packageSize
            return when {
                statePercent > STATE_GOOD_LIMIT -> Type.GOOD
                statePercent > STATE_MEDIUM_LIMIT -> Type.MEDIUM
                statePercent > 0 -> Type.SMALL
                else -> Type.EMPTY
            }
        }

    fun reduce(doseSize: Float) {
        val currState = this.currState
        var newState = currState - doseSize
        if (newState < 0f) {
            newState = 0f
        }
        this.currState = newState
    }

    fun increase(doseSize: Float) {
        val currState = this.currState
        val packageSize = this.packageSize
        var newState = currState + doseSize
        if (newState > packageSize) {
            newState = packageSize
        }
        this.currState = newState
    }

    enum class Type {
        EMPTY, SMALL, MEDIUM, GOOD
    }

    companion object {
        private const val STATE_GOOD_LIMIT = 0.75f
        private const val STATE_MEDIUM_LIMIT = 0.4f
    }
}