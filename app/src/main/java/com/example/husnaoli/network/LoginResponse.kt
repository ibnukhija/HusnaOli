package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String
)
