package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class DetailTransaksiResponse(
    val status: String,
    val data: List<DetailItem>
)

data class DetailItem(
    @SerializedName("nama_item") val namaItem: String,
    val jumlah: Int,
    val harga: Int
)