package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_barang") val totalBarang: Int,
    @SerializedName("stok_tipis") val stokTipis: Int,
    @SerializedName("omset") val omset: Double
)
