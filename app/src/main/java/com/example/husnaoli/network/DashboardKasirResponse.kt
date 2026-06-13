package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class DashboardKasirResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_pendapatan") val totalPendapatan: Int,
    @SerializedName("jumlah_transaksi") val jumlahTransaksi: Int,
    @SerializedName("riwayat") val riwayat: List<KasirHistoryData>
)

data class KasirHistoryData(
    @SerializedName("id") val id: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("detail") val detail: String?
)
