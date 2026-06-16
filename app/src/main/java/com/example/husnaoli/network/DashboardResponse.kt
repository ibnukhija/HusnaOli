package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_barang") val totalBarang: Int,
    @SerializedName("stok_tipis") val stokTipis: Int,
    @SerializedName("omset") val omset: Double,
    @SerializedName("transaksi_terakhir") val transaksiTerakhir: List<TransaksiTerakhir>?
)

data class TransaksiTerakhir(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("total") val total: Double,
    @SerializedName("detail") val detail: String?,
    @SerializedName("kasir") val kasir: String?
)
