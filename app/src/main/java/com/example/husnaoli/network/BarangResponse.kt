package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class BarangResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<BarangData>
)

data class BarangData(
    @SerializedName("item_id") val itemId: Int,
    @SerializedName("nama_item") val namaItem: String,
    @SerializedName("nama_kategori") val namaKategori: String,
    @SerializedName("harga_beli") val hargaBeli: Int,
    @SerializedName("harga_jual") val hargaJual: Int,
    @SerializedName("stok") val stok: Int,
    @SerializedName("foto") val foto: String?
)
