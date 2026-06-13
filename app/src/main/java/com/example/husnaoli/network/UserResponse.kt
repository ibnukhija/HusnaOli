package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<UserData>
)
