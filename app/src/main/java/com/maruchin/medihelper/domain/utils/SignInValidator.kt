package com.maruchin.medihelper.domain.utils

class SignInValidator {

    fun validate(params: Params): Errors {
        val errors = Errors()

        if (params.email.isNullOrEmpty()) {
            errors.emptyEmail = true
        }
        if (params.password.isNullOrEmpty()) {
            errors.emptyPassword
        }
        return errors
    }

    data class Params(
        val email: String?,
        val password: String?
    )

    data class Errors(
        var emptyEmail: Boolean = false,
        var emptyPassword: Boolean = false
    ) {
        val noErrors: Boolean
            get() = arrayOf(
                emptyEmail,
                emptyEmail
            ).any { it }
    }
}