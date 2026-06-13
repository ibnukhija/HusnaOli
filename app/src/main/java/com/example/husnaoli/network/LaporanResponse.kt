package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class LaporanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<LaporanData>
)

data class LaporanData(
    @SerializedName("id") val id: Int?,
    @SerializedName("tanggal") val tanggal: String?,
    @SerializedName("total") val total: Int?,
    @SerializedName("kasir") val kasir: String?,
    @SerializedName("detail") val detail: String?
)