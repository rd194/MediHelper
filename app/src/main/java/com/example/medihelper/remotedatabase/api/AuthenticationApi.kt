package com.example.medihelper.remotedatabase.api

import com.example.medihelper.remotedatabase.dto.ConnectedPersonDto
import com.example.medihelper.remotedatabase.dto.LoginResponseDto
import com.example.medihelper.remotedatabase.dto.UserCredentialsDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthenticationApi {

    @POST("authentication/login")
    suspend fun loginUser(@Body userCredentialsDto: UserCredentialsDto): LoginResponseDto

    @GET("authentication/patron-connect")
    suspend fun patronConnect(@Query("connectionKey") connectionKey: String): ConnectedPersonDto

    @POST("authentication/register")
    suspend fun registerNewUser(@Body userCredentialsDto: UserCredentialsDto)
}