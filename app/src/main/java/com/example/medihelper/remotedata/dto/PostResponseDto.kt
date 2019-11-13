package com.example.medihelper.remotedata.dto

import com.google.gson.annotations.SerializedName

data class PostResponseDto(
    @SerializedName(value = "localId")
    val localId: Int,

    @SerializedName(value = "remoteId")
    val remoteId: Long
)