package com.example.medihelper.mainapp.more.loginregister

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medihelper.custom.ActionLiveData
import com.example.medihelper.remotedatabase.remoterepositories.RegisteredUserRemoteRepository
import com.example.medihelper.remotedatabase.ApiResponse
import com.example.medihelper.remotedatabase.pojos.registereduser.UserCredentialsDto
import com.example.medihelper.services.ServerSyncService
import com.example.medihelper.services.SharedPrefService
import kotlinx.coroutines.launch

class LoginRegisterViewModel(
    private val registeredUserRemoteRepository: RegisteredUserRemoteRepository,
    private val sharedPrefService: SharedPrefService,
    private val serverSyncService: ServerSyncService
) : ViewModel() {
    private val TAG = "LoginRegisterViewModel"

    val emailLive = MutableLiveData<String>()
    val passwordLive = MutableLiveData<String>()
    val passwordConfirmationLive = MutableLiveData<String>()
    val errorEmailLive = MutableLiveData<String>()
    val errorPasswordLive = MutableLiveData<String>()
    val errorPasswordConfirmationLive = MutableLiveData<String>()
    val loadingStartedAction = ActionLiveData<Boolean>()
    val loginResponseAction = ActionLiveData<ApiResponse>()
    val registrationResponseAction = ActionLiveData<ApiResponse>()


    fun resetViewModel() {
        listOf(
            emailLive,
            passwordLive,
            passwordConfirmationLive,
            errorEmailLive,
            errorPasswordLive,
            errorPasswordConfirmationLive
        ).forEach { liveData ->
            liveData.postValue(null)
        }
    }

    fun loginUser() = viewModelScope.launch {
        if (validateInputData(Mode.LOGIN)) {
            loadingStartedAction.postValue(true)
            val userCredentials = UserCredentialsDto(
                email = emailLive.value!!,
                password = passwordLive.value!!
            )
            val response = try {
                val tempAuthToken = registeredUserRemoteRepository.loginUser(userCredentials)
                val hasRemoteData = registeredUserRemoteRepository.hasData(tempAuthToken)
                if (!hasRemoteData) {
                    serverSyncService.overwriteRemote(tempAuthToken)
                } else {
                    serverSyncService.overwriteLocal(tempAuthToken)
                }
                sharedPrefService.run {
                    saveLoggedUserAuthToken(tempAuthToken)
                    saveLoggedUserEmail(userCredentials.email)
                }
                ApiResponse.OK
            } catch (e: Exception) {
                Log.i(TAG, "login error = $e")
                e.printStackTrace()
                ApiResponse.getResponseByException(e)
            }
            loginResponseAction.sendAction(response)
        }
    }

    fun registerNewUser() = viewModelScope.launch {
        if (validateInputData(Mode.REGISTER)) {
            loadingStartedAction.postValue(true)
            val userCredentials = UserCredentialsDto(
                email = emailLive.value!!,
                password = passwordLive.value!!
            )
            val response = try {
                registeredUserRemoteRepository.registerNewUser(userCredentials)
                ApiResponse.OK
            } catch (e: Exception) {
                Log.i(TAG, "register error = $e")
                ApiResponse.getResponseByException(e)
            }
            registrationResponseAction.sendAction(response)
        }
    }

    private fun validateInputData(viewModelMode: Mode): Boolean {
        var inputDataValid = true
        if (emailLive.value.isNullOrEmpty()) {
            errorEmailLive.postValue("Adres e-mail jest wymagany")
            inputDataValid = false
        } else {
            errorEmailLive.postValue(null)
        }
        if (passwordLive.value.isNullOrEmpty()) {
            errorPasswordLive.postValue("Hasło jest wymagane")
            inputDataValid = false
        } else {
            errorPasswordLive.postValue(null)
        }
        if (viewModelMode == Mode.REGISTER) {
            if (passwordConfirmationLive.value.isNullOrEmpty()) {
                errorPasswordConfirmationLive.postValue("Potwierdzenie hasła jest wymagane")
                inputDataValid = false
            } else {
                errorPasswordConfirmationLive.postValue(null)
            }
            if (!passwordLive.value.isNullOrEmpty() && !passwordConfirmationLive.value.isNullOrEmpty()) {
                if (passwordLive.value != passwordConfirmationLive.value) {
                    val errorMessage = "Hasła nie są takie same"
                    errorPasswordLive.postValue(errorMessage)
                    errorPasswordConfirmationLive.postValue(errorMessage)
                    inputDataValid = false
                } else {
                    errorPasswordLive.postValue(null)
                    errorPasswordConfirmationLive.postValue(null)
                }
            }
        }
        return inputDataValid
    }

    enum class Mode {
        LOGIN, REGISTER
    }
}