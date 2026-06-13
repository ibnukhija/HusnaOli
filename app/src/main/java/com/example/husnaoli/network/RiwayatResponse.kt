package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class RiwayatResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<RiwayatData>
)

data class RiwayatData(
    @SerializedName("restock_id") val restockId: Int,
    @SerializedName("tanggal_masuk") val tanggalMasuk: String,
    @SerializedName("nama_toko") val namaToko: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("total") val total: Int?,
    @SerializedName("harga_beli_saat_itu") val hargaBeli: Int?,
    @SerializedName("jumlah") val jumlah: Int?
)
