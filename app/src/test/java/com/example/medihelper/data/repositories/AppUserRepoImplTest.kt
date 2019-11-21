package com.example.medihelper.data.repositories

import android.content.Context
import com.example.medihelper.data.local.DeletedHistory
import com.example.medihelper.data.local.SharedPref
import com.example.medihelper.data.remote.ApiResponseMapper
import com.example.medihelper.data.remote.api.AuthenticationApi
import com.example.medihelper.data.remote.api.RegisteredUserApi
import com.example.medihelper.data.remote.dto.LoginResponseDto
import com.example.medihelper.data.remote.dto.UserCredentialsDto
import com.example.medihelper.domain.entities.ApiResponse
import com.example.medihelper.domain.entities.AppMode
import com.example.medihelper.domain.repositories.AppUserRepo
import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppUserRepoImplTest : KoinTest {

    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var sharedPref: SharedPref
    @Mock
    private lateinit var authenticationApi: AuthenticationApi
    @Mock
    private lateinit var registeredUserApi: RegisteredUserApi
    @Mock
    private lateinit var apiResponseMapper: ApiResponseMapper
    @Mock
    private lateinit var deletedHistory: DeletedHistory

    private val testModule = module {
        single {
            AppUserRepoImpl(
                context, sharedPref, authenticationApi, registeredUserApi, apiResponseMapper, deletedHistory
            ) as AppUserRepo
        }
    }

    private val appUserRepo: AppUserRepo by inject()

    @Before
    fun before() {
        startKoin {
            modules(testModule)
        }
    }

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun getAppMode_NoTokenAndNoEmail_ReturnOffline() {
        Mockito.`when`(sharedPref.getAuthToken()).thenReturn(null)
        Mockito.`when`(sharedPref.getUserEmail()).thenReturn(null)

        val appMode = appUserRepo.getAppMode()
        Truth.assertThat(appMode).isEqualTo(AppMode.OFFLINE)
    }

    @Test
    fun getAppMode_TokenAndEmail_ReturnLogged() {
        Mockito.`when`(sharedPref.getAuthToken()).thenReturn("token")
        Mockito.`when`(sharedPref.getUserEmail()).thenReturn("test@mail.com")

        val appMode = appUserRepo.getAppMode()
        Truth.assertThat(appMode).isEqualTo(AppMode.LOGGED)
    }

    @Test
    fun loginUser_Success_NoRemoteData() {
        runBlocking {
            val email = "test@email.com"
            val password = "abc"

            val mockedOutput = LoginResponseDto(
                authToken = "auth-token",
                isDataAvailable = false
            )
            Mockito.`when`(authenticationApi.loginUser(UserCredentialsDto(email, password))).thenReturn(mockedOutput)

            val returnedPair = appUserRepo.loginUser(email, password)

            val apiResponse = returnedPair.first
            val isDataAvailable = returnedPair.second

            Truth.assertThat(apiResponse).isEqualTo(ApiResponse.OK)
            Truth.assertThat(isDataAvailable).isFalse()
        }
    }
}